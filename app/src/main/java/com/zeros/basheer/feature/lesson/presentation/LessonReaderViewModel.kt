package com.zeros.basheer.feature.lesson.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.progress.domain.model.UserProgress
import com.zeros.basheer.feature.lesson.domain.repository.LessonRepository
import com.zeros.basheer.feature.lesson.data.mapper.LessonMapper
import com.zeros.basheer.core.domain.model.Result
import com.zeros.basheer.domain.model.LessonContent
import com.zeros.basheer.feature.concept.domain.model.Concept
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
//import com.zeros.basheer.feature.lesson.domain.model.LessonContent
import com.zeros.basheer.feature.lesson.domain.usecase.GetLessonContentUseCase
import com.zeros.basheer.feature.progress.domain.usecase.MarkLessonCompleteUseCase
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import com.zeros.basheer.feature.streak.domain.usecase.RecordLessonCompletedUseCase
import com.zeros.basheer.feature.streak.domain.usecase.RecordTimeSpentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LessonReaderState(
    val lessonContent: LessonContent? = null,
    val progress: UserProgress? = null,
    val isLoading: Boolean = true,
    val error: String? = null,

    // Concept modal
    val activeConcept: Concept? = null,

    // Progress tracking
    val readingTimeSeconds: Long = 0,
    val scrollProgress: Float = 0f,
    val hasReachedEnd: Boolean = false,

    // UI state
    val showProgressOverlay: Boolean = false
)

@HiltViewModel
class LessonReaderViewModel @Inject constructor(
    private val repository: LessonRepository,
    private val getLessonContentUseCase: GetLessonContentUseCase,
    private val markLessonCompleteUseCase: MarkLessonCompleteUseCase,
    private val progressRepository: ProgressRepository,
    private val conceptRepository: ConceptRepository,
    private val recordLessonCompletedUseCase: RecordLessonCompletedUseCase,
    private val recordTimeSpentUseCase: RecordTimeSpentUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _state = MutableStateFlow(LessonReaderState())
    val state: StateFlow<LessonReaderState> = _state.asStateFlow()

    private var timeTrackingJob: Job? = null
    private var isTrackingTime = false

    init {
        loadLesson()
        loadProgress()
    }

    private fun loadLesson() {
        viewModelScope.launch {
            // Use NEW use case
            when (val result = getLessonContentUseCase(lessonId)) {
                is Result.Success -> {
                    _state.update { it.copy(
                        lessonContent = result.data,
                        isLoading = false
                    )}
                }
                is Result.Error -> {
                    _state.update { it.copy(
                        error = result.message,
                        isLoading = false
                    )}
                }
            }
        }
    }

    private fun loadProgress() {
        viewModelScope.launch {
            // Observe progress changes
            progressRepository.getProgressByLesson(lessonId).collect { progress ->
                _state.update { it.copy(progress = progress) }
            }
        }
    }
//    private fun loadLesson() {
//        viewModelScope.launch {
//            try {
//                // Load full lesson with sections and blocks
//                val lessonFull = repository.getLessonFull(lessonId)
//                if (lessonFull == null) {
//                    _state.update { it.copy(
//                        error = "الدرس غير موجود",
//                        isLoading = false
//                    )}
//                    return@launch
//                }
//
//                // Map to UI model
//                val lessonContent = LessonMapper.toLessonContent(lessonFull)
//
//                // Load progress
//                repository.getProgressByLesson(lessonId).collect { progress ->
//                    _state.update { it.copy(
//                        lessonContent = lessonContent,
//                        progress = progress,
//                        isLoading = false
//                    )}
//                }
//
//                // Update last accessed time
//                updateLastAccessed()
//            } catch (e: Exception) {
//                _state.update { it.copy(
//                    error = e.message ?: "حدث خطأ",
//                    isLoading = false
//                )}
//            }
//        }
//    }

    private fun updateLastAccessed() {
        viewModelScope.launch {
            val currentProgress = _state.value.progress
            if (currentProgress != null) {
                progressRepository.updateProgress(
                    currentProgress.copy(lastAccessedAt = System.currentTimeMillis())
                )
            } else {
                progressRepository.updateProgress(
                    UserProgress(
                        lessonId = lessonId,
                        lastAccessedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // ==================== Time Tracking ====================

    fun startTimeTracking() {
        if (isTrackingTime) return
        isTrackingTime = true

        // Update last accessed when starting to read
        updateLastAccessed()

        timeTrackingJob = viewModelScope.launch {
            while (isActive && isTrackingTime) {
                delay(1000)
                _state.update { it.copy(
                    readingTimeSeconds = it.readingTimeSeconds + 1
                )}
            }
        }
    }

    fun pauseTimeTracking() {
        isTrackingTime = false
        timeTrackingJob?.cancel()
        timeTrackingJob = null

        // Save reading time to progress
        saveReadingTime()
    }

    private fun saveReadingTime() {
        viewModelScope.launch {
            val timeSpent = _state.value.readingTimeSeconds

            if (timeSpent > 0) {
                // Record time in progress
                val currentProgress = _state.value.progress ?: UserProgress(lessonId = lessonId)
                val totalTime = currentProgress.timeSpentSeconds + timeSpent.toInt()
                progressRepository.updateProgress(
                    currentProgress.copy(timeSpentSeconds = totalTime)
                )

                // Record time in streak/daily activity
                recordTimeSpentUseCase(timeSpent)
            }
        }
    }

    // ==================== Scroll Progress ====================

    fun onScrollProgressChanged(progress: Float) {
        _state.update { it.copy(scrollProgress = progress) }

        // Check if user has reached the end (>95%)
        if (progress >= 0.95f && !_state.value.hasReachedEnd) {
            _state.update { it.copy(hasReachedEnd = true) }
        }
    }

    // ==================== Concept Modal ====================

    fun onConceptClick(conceptId: String) {
        viewModelScope.launch {
            val concept = conceptRepository.getConceptById(conceptId)
            _state.update { it.copy(activeConcept = concept) }
        }
    }

    fun dismissConceptModal() {
        _state.update { it.copy(activeConcept = null) }
    }

    // ==================== Actions ====================

    fun markAsCompleted() {
        viewModelScope.launch {
            // Save reading time to both progress and streak
            saveReadingTime()

            // Mark lesson as complete in progress
            markLessonCompleteUseCase(lessonId)

            // Record lesson completion in streak/daily activity
            recordLessonCompletedUseCase()
        }
    }


    fun toggleProgressOverlay() {
        _state.update { it.copy(showProgressOverlay = !it.showProgressOverlay) }
    }

    override fun onCleared() {
        super.onCleared()
        pauseTimeTracking()
    }
}