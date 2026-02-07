package com.zeros.basheer.ui.screens.quizbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.data.models.*
import com.zeros.basheer.data.repository.QuestionCounts
import com.zeros.basheer.data.repository.QuizBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Quiz Bank Screen
 */
data class QuizBankState(
    // Exams by source
    val ministryExams: List<Exam> = emptyList(),
    val schoolExams: List<Exam> = emptyList(),
    val practiceExams: List<Exam> = emptyList(),

    // Practice sessions
    val recentSessions: List<PracticeSession> = emptyList(),
    val activeSession: PracticeSession? = null,

    // Question counts
    val questionCounts: QuestionCounts? = null,

    // Stats
    val averageScore: Float? = null,
    val completedSessionCount: Int = 0,

    // UI state
    val selectedTab: QuizBankTab = QuizBankTab.MINISTRY_EXAMS,
    val isLoading: Boolean = true,
    val error: String? = null
)


/**
 * Tabs for Quiz Bank Screen
 */
enum class QuizBankTab {
    MINISTRY_EXAMS,     // امتحانات الوزارة
    SCHOOL_EXAMS,       // امتحانات المدارس
    PRACTICE_MODES,     // أوضاع التدريب
    HISTORY             // السجل
}

/**
 * Events from Quiz Bank Screen
 */
sealed class QuizBankEvent {
    data class SelectTab(val tab: QuizBankTab) : QuizBankEvent()
    data class StartExam(val examId: String) : QuizBankEvent()
    data class StartPracticeSession(
        val generationType: PracticeGenerationType,
        val questionCount: Int = 20,
        val filterUnitIds: List<String>? = null,
        val filterConceptIds: List<String>? = null,
        val filterQuestionTypes: List<QuestionType>? = null,
        val filterDifficulty: IntRange? = null
    ) : QuizBankEvent()
    data class ResumeSession(val sessionId: Long) : QuizBankEvent()
    object Refresh : QuizBankEvent()
}



@HiltViewModel
class QuizBankViewModel @Inject constructor(
    private val quizBankRepository: QuizBankRepository
) : ViewModel() {
    sealed class NavigationEvent {
        data class NavigateToPractice(val sessionId: Long) : NavigationEvent()
    }

    private val _state = MutableStateFlow(QuizBankState())
    val state: StateFlow<QuizBankState> = _state.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    // Current subject (for now, hardcoded to Geography)
    private val currentSubjectId = "geography"

    init {
        loadData()
    }

    /**
     * Handle UI events
     */
    fun onEvent(event: QuizBankEvent) {
        when (event) {
            is QuizBankEvent.SelectTab -> {
                _state.update { it.copy(selectedTab = event.tab) }
            }
            is QuizBankEvent.StartExam -> {
                startExam(event.examId)
            }
            is QuizBankEvent.StartPracticeSession -> {
                startPracticeSession(
                    generationType = event.generationType,
                    questionCount = event.questionCount,
                    filterUnitIds = event.filterUnitIds,
                    filterConceptIds = event.filterConceptIds,
                    filterQuestionTypes = event.filterQuestionTypes,
                    filterDifficulty = event.filterDifficulty
                )
            }
            is QuizBankEvent.ResumeSession -> {
                resumeSession(event.sessionId)
            }
            QuizBankEvent.Refresh -> {
                loadData()
            }
        }
    }

    /**
     * Load all data for Quiz Bank
     */
    private fun loadData() {
        _state.update { it.copy(isLoading = true, error = null) }

        // Load ministry exams
        viewModelScope.launch {
            quizBankRepository.getExamsBySource(ExamSource.MINISTRY)
                .catch { e ->
                    _state.update { it.copy(error = e.message) }
                }
                .collect { exams ->
                    _state.update { it.copy(ministryExams = exams) }
                }
        }

        // Load school exams
        viewModelScope.launch {
            quizBankRepository.getExamsBySource(ExamSource.SCHOOL)
                .catch { e ->
                    _state.update { it.copy(error = e.message) }
                }
                .collect { exams ->
                    _state.update { it.copy(schoolExams = exams) }
                }
        }

        // Load practice exams
        viewModelScope.launch {
            quizBankRepository.getExamsBySource(ExamSource.PRACTICE)
                .catch { e ->
                    _state.update { it.copy(error = e.message) }
                }
                .collect { exams ->
                    _state.update { it.copy(practiceExams = exams) }
                }
        }

        // Load recent practice sessions
        viewModelScope.launch {
            quizBankRepository.getRecentSessions(10)
                .catch { e ->
                    _state.update { it.copy(error = e.message) }
                }
                .collect { sessions ->
                    _state.update { it.copy(recentSessions = sessions) }
                }
        }

        // Load active session
        viewModelScope.launch {
            val activeSession = quizBankRepository.getActiveSession()
            _state.update { it.copy(activeSession = activeSession) }
        }

        // Load question counts
        viewModelScope.launch {
            val counts = quizBankRepository.getQuestionCounts(currentSubjectId)
            _state.update { it.copy(questionCounts = counts) }
        }

        // Load stats
        viewModelScope.launch {
            quizBankRepository.getAverageScoreForSubject(currentSubjectId)
                .catch { e ->
                    _state.update { it.copy(error = e.message) }
                }
                .collect { score ->
                    _state.update { it.copy(averageScore = score, isLoading = false) }
                }
        }

        viewModelScope.launch {
            quizBankRepository.getCompletedSessionCount(currentSubjectId)
                .catch { e ->
                    _state.update { it.copy(error = e.message) }
                }
                .collect { count ->
                    _state.update { it.copy(completedSessionCount = count) }
                }
        }
    }

    /**
     * Start a full exam (not implemented yet - navigate to exam screen)
     */
    private fun startExam(examId: String) {
        // TODO: Navigate to exam screen
        // For now, this is just a placeholder
        viewModelScope.launch {
            try {
                val exam = quizBankRepository.getExamById(examId)
                // Navigate to exam screen with exam data
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Start a new practice session
     */
    private fun startPracticeSession(
        generationType: PracticeGenerationType,
        questionCount: Int = 20,
        filterUnitIds: List<String>? = null,
        filterConceptIds: List<String>? = null,
        filterQuestionTypes: List<QuestionType>? = null,
        filterDifficulty: IntRange? = null
    ) {
        viewModelScope.launch {
            try {
                val sessionId = quizBankRepository.createPracticeSession(
                    subjectId = currentSubjectId,
                    generationType = generationType,
                    questionCount = questionCount,
                    filterUnitIds = filterUnitIds,
                    filterConceptIds = filterConceptIds,
                    filterQuestionTypes = filterQuestionTypes,
                    filterDifficulty = filterDifficulty
                )
                _navigationEvent.emit(NavigationEvent.NavigateToPractice(sessionId))


                // TODO: Navigate to practice session screen with sessionId
                // Reload active session
                val activeSession = quizBankRepository.getActiveSession()
                _state.update { it.copy(activeSession = activeSession) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Resume an existing practice session
     */
    private fun resumeSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                val session = quizBankRepository.getSession(sessionId)
                if (session != null) {
                    // TODO: Navigate to practice session screen with sessionId
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}