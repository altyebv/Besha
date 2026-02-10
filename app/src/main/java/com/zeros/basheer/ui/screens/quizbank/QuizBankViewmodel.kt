package com.zeros.basheer.ui.screens.quizbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.practice.domain.model.PracticeGenerationType
import com.zeros.basheer.feature.practice.domain.model.PracticeSession
import com.zeros.basheer.feature.practice.domain.repository.PracticeRepository
import com.zeros.basheer.feature.quizbank.domain.model.Exam
import com.zeros.basheer.feature.quizbank.domain.model.ExamSource
import com.zeros.basheer.feature.quizbank.domain.model.QuestionCounts
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizBankState(
    val ministryExams: List<Exam> = emptyList(),
    val schoolExams: List<Exam> = emptyList(),
    val practiceExams: List<Exam> = emptyList(),
    val recentSessions: List<PracticeSession> = emptyList(),
    val activeSession: PracticeSession? = null,
    val questionCounts: QuestionCounts? = null,
    val averageScore: Float? = null,
    val completedSessionCount: Int = 0,
    val selectedTab: QuizBankTab = QuizBankTab.MINISTRY_EXAMS,
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class QuizBankTab {
    MINISTRY_EXAMS,
    SCHOOL_EXAMS,
    PRACTICE_MODES,
    HISTORY
}

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
    private val quizBankRepository: QuizBankRepository,
    private val practiceRepository: PracticeRepository  // ADD THIS
) : ViewModel() {

    sealed class NavigationEvent {
        data class NavigateToPractice(val sessionId: Long) : NavigationEvent()
    }

    private val _state = MutableStateFlow(QuizBankState())
    val state: StateFlow<QuizBankState> = _state.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val currentSubjectId = "geography"

    init {
        loadData()
    }

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

    private fun loadData() {
        _state.update { it.copy(isLoading = true, error = null) }

        // Load ministry exams - FIX SYNTAX ERROR
        viewModelScope.launch {
            quizBankRepository.getExamsBySource(currentSubjectId, ExamSource.MINISTRY)
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { exams -> _state.update { it.copy(ministryExams = exams) } }
        }

        // Load school exams - FIX SYNTAX ERROR
        viewModelScope.launch {
            quizBankRepository.getExamsBySource(currentSubjectId, ExamSource.SCHOOL)
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { exams -> _state.update { it.copy(schoolExams = exams) } }
        }

        // Load practice exams - FIX SYNTAX ERROR
        viewModelScope.launch {
            quizBankRepository.getExamsBySource(currentSubjectId, ExamSource.PRACTICE)
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { exams -> _state.update { it.copy(practiceExams = exams) } }
        }

        // Load recent practice sessions - USE PRACTICE REPOSITORY
        viewModelScope.launch {
            practiceRepository.getRecentCompletedSessions(10)
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { sessions -> _state.update { it.copy(recentSessions = sessions) } }
        }

        // Load active session - USE PRACTICE REPOSITORY
        viewModelScope.launch {
            val activeSession = practiceRepository.getActiveSession()
            _state.update { it.copy(activeSession = activeSession) }
        }

        // Load question counts
        viewModelScope.launch {
            val counts = quizBankRepository.getQuestionCounts(currentSubjectId)
            _state.update { it.copy(questionCounts = counts) }
        }

        // Load stats
        viewModelScope.launch {
            practiceRepository.getAverageScore(currentSubjectId)
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { score -> _state.update { it.copy(averageScore = score, isLoading = false) } }
        }

        viewModelScope.launch {
            practiceRepository.getCompletedSessionCount(currentSubjectId)
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { count -> _state.update { it.copy(completedSessionCount = count) } }
        }
    }

    private fun startExam(examId: String) {
        viewModelScope.launch {
            try {
                val exam = quizBankRepository.getExamById(examId)
                // TODO: Navigate to exam screen
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

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
                val sessionId = practiceRepository.createPracticeSession(
                    subjectId = currentSubjectId,
                    generationType = generationType,
                    questionCount = questionCount,
                    filterUnitIds = filterUnitIds,
                    filterConceptIds = filterConceptIds,
                    filterQuestionTypes = filterQuestionTypes,
                    filterDifficulty = filterDifficulty
                )
                _navigationEvent.emit(NavigationEvent.NavigateToPractice(sessionId))

                val activeSession = practiceRepository.getActiveSession()
                _state.update { it.copy(activeSession = activeSession) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    private fun resumeSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                val session = practiceRepository.getSession(sessionId)
                if (session != null) {
                    _navigationEvent.emit(NavigationEvent.NavigateToPractice(sessionId))
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}