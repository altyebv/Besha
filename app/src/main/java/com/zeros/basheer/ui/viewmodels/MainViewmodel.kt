package com.zeros.basheer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.streak.domain.model.DailyActivity
import com.zeros.basheer.domain.model.ScoredRecommendation
import com.zeros.basheer.feature.streak.data.entity.StreakLevel
import com.zeros.basheer.feature.streak.domain.model.StreakStatus
import com.zeros.basheer.data.repository.LessonRepository
import com.zeros.basheer.domain.recommendation.RecommendationEngine
import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import com.zeros.basheer.feature.streak.domain.usecase.GetStreakStatusUseCase
import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.subject.domain.model.Units
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectWithProgress(
    val subject: Subject,
    val totalLessons: Int,
    val completedLessons: Int,
    val units: List<Units>,
    val score: Float = 0f, // For smart ordering
    val nextLessonTitle: String? = null,
    val lastStudied: Long? = null
)

data class MainScreenState(
    val subjects: List<SubjectWithProgress> = emptyList(),
    val completedLessonsCount: Int = 0,
    val totalLessonsCount: Int = 0,
    val overallProgress: Float = 0f,
    val isLoading: Boolean = true,

    // Streak data
    val streakStatus: StreakStatus = StreakStatus(
        currentStreak = 0,
        longestStreak = 0,
        todayLevel = StreakLevel.COLD,
        lastActiveDate = null,
        isAtRisk = false
    ),
    val todayActivity: DailyActivity? = null,

    // Smart recommendations
    val topRecommendation: ScoredRecommendation? = null,
    val secondaryRecommendations: List<ScoredRecommendation> = emptyList(),
    val focusCardDismissed: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: LessonRepository,
    private val recommendationEngine: RecommendationEngine,
    private val getStreakStatusUseCase: GetStreakStatusUseCase,
    private val streakRepository: StreakRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    init {
        loadData()
        observeStreakStatus()
        observeTodayActivity()
        loadRecommendations()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Start with loading state
            _state.update { it.copy(isLoading = true) }

            // Collect all subjects
            repository.getAllSubjects().collect { subjects ->
                val subjectsWithProgress = mutableListOf<SubjectWithProgress>()
                var totalLessons = 0
                var totalCompleted = 0

                // For each subject, calculate its progress
                for (subject in subjects) {
                    // Get units
                    val units = repository.getUnitsBySubject(subject.id).first()

                    // Get lessons
                    val lessons = repository.getLessonsBySubject(subject.id).first()
                    totalLessons += lessons.size

                    // Get completed lessons
                    val completedLessons = repository.getCompletedLessons().first()

                    // Count completed lessons for this subject
                    val completedCount = completedLessons.count { progress ->
                        lessons.any { it.id == progress.lessonId }
                    }
                    totalCompleted += completedCount

                    // Find next lesson
                    val recentLessons = repository.getRecentlyAccessedLessons(10).first()
                    val nextLesson = recentLessons
                        .filter { progress -> lessons.any { it.id == progress.lessonId } }
                        .firstOrNull { progress -> !progress.completed }
                        ?.let { progress -> lessons.find { it.id == progress.lessonId } }

                    val lastStudied = recentLessons
                        .filter { progress -> lessons.any { it.id == progress.lessonId } }
                        .maxByOrNull { it.lastAccessedAt }
                        ?.lastAccessedAt

                    subjectsWithProgress.add(
                        SubjectWithProgress(
                            subject = subject,
                            totalLessons = lessons.size,
                            completedLessons = completedCount,
                            units = units,
                            nextLessonTitle = nextLesson?.title,
                            lastStudied = lastStudied
                        )
                    )
                }

                // Calculate overall progress
                val overallProgress = if (totalLessons > 0) {
                    totalCompleted.toFloat() / totalLessons
                } else 0f

                // Update state
                _state.update { current ->
                    current.copy(
                        subjects = subjectsWithProgress,
                        completedLessonsCount = totalCompleted,
                        totalLessonsCount = totalLessons,
                        overallProgress = overallProgress,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeStreakStatus() {
        viewModelScope.launch {
            getStreakStatusUseCase.asFlow().collect { status ->  // NEW
                _state.update { it.copy(streakStatus = status) }
            }
        }
    }

    private fun observeTodayActivity() {
        viewModelScope.launch {
            streakRepository.getTodayActivityFlow().collect { activity ->  // NEW
                _state.update { it.copy(todayActivity = activity) }
            }
        }
    }

    /**
     * Load smart recommendations from engine
     */
    private fun loadRecommendations() {
        viewModelScope.launch {
            try {
                val recommendations = recommendationEngine.getTopRecommendations(limit = 3)

                _state.update { current ->
                    current.copy(
                        topRecommendation = recommendations.firstOrNull(),
                        secondaryRecommendations = recommendations.drop(1)
                    )
                }

                // Update subject ordering based on recommendation scores
                updateSubjectOrdering(recommendations)
            } catch (e: Exception) {
                // If recommendations fail, don't crash - just continue without them
                e.printStackTrace()
            }
        }
    }

    /**
     * Update subject card ordering based on recommendation scores
     */
    private fun updateSubjectOrdering(recommendations: List<ScoredRecommendation>) {
        _state.update { current ->
            val scoreMap = recommendations
                .groupBy { it.subject.id }
                .mapValues { (_, recs) -> recs.maxOf { it.score } }

            val orderedSubjects = current.subjects
                .map { it.copy(score = scoreMap[it.subject.id] ?: 0f) }
                .sortedByDescending { it.score }

            current.copy(subjects = orderedSubjects)
        }
    }

    /**
     * Dismiss the top recommendation (show next one tomorrow)
     */
    fun dismissFocusCard() {
        _state.update { it.copy(focusCardDismissed = true) }
        // TODO: Persist dismissal to preferences
    }

    fun refreshData() {
        loadData()
        loadRecommendations()
    }
}