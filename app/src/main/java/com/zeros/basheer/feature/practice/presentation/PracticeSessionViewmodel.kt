package com.zeros.basheer.feature.practice.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.practice.presentation.components.QuestionInteractionState
import com.zeros.basheer.feature.practice.domain.model.PracticeSession
import com.zeros.basheer.feature.practice.domain.model.PracticeSessionStatus
import com.zeros.basheer.feature.practice.domain.repository.PracticeRepository
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.model.QuestionStats
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import com.zeros.basheer.feature.user.domain.model.XpSource
import com.zeros.basheer.feature.user.domain.usecase.AwardXpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Practice Session
 */
data class PracticeSessionState(
    val session: PracticeSession? = null,
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val interactionState: QuestionInteractionState = QuestionInteractionState.Idle,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isComplete: Boolean = false,

    // Timer
    val questionStartTime: Long = System.currentTimeMillis(),

    // Stats for current session
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val skippedCount: Int = 0
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentQuestionIndex)
    val progress: Float get() = if (questions.isNotEmpty()) {
        currentQuestionIndex.toFloat() / questions.size
    } else 0f
    val questionsRemaining: Int get() = questions.size - currentQuestionIndex
}

/**
 * Events for Practice Session
 */
sealed class PracticeSessionEvent {
    data class AnswerQuestion(val answer: String) : PracticeSessionEvent()
    object ContinueToNext : PracticeSessionEvent()
    object SkipQuestion : PracticeSessionEvent()
    object ExitSession : PracticeSessionEvent()
    object RetrySession : PracticeSessionEvent()
}

/** One-shot navigation events emitted by PracticeSessionViewModel. */
sealed class PracticeNavEvent {
    /** Navigate to a newly-created practice session, replacing the current one. */
    data class NavigateToSession(val sessionId: Long) : PracticeNavEvent()
}

@HiltViewModel
class PracticeSessionViewModel @Inject constructor(
    private val quizBankRepository: QuizBankRepository,
    private val practiceRepository: PracticeRepository,
    private val streakRepository: StreakRepository,
    private val awardXpUseCase: AwardXpUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: Long = savedStateHandle.get<Long>("sessionId")
        ?: throw IllegalArgumentException("Session ID is required")

    private val _state = MutableStateFlow(PracticeSessionState())
    val state: StateFlow<PracticeSessionState> = _state.asStateFlow()

    // One-shot navigation events (e.g. navigate to a newly-created retry session)
    private val _navigationEvent = MutableSharedFlow<PracticeNavEvent>(replay = 0)
    val navigationEvent: SharedFlow<PracticeNavEvent> = _navigationEvent.asSharedFlow()

    init {
        loadSession()
    }

    /**
     * Handle UI events
     */
    fun onEvent(event: PracticeSessionEvent) {
        when (event) {
            is PracticeSessionEvent.AnswerQuestion -> answerQuestion(event.answer)
            PracticeSessionEvent.ContinueToNext -> continueToNext()
            PracticeSessionEvent.SkipQuestion -> skipQuestion()
            PracticeSessionEvent.ExitSession -> exitSession()
            PracticeSessionEvent.RetrySession -> retrySession()
        }
    }

    /**
     * Load session and questions
     */
    private fun loadSession() {
        viewModelScope.launch {
            try {
                val session = practiceRepository.getSession(sessionId)
                if (session == null) {
                    _state.update { it.copy(error = "Session not found", isLoading = false) }
                    return@launch
                }

                val sessionQuestions = practiceRepository.getQuestionsForSession(sessionId)
                val questionIds = sessionQuestions.map { it.questionId }

                // Load actual questions
                val questions = questionIds.mapNotNull { questionId ->
                    quizBankRepository.getQuestionById(questionId)
                }

                _state.update {
                    it.copy(
                        session = session,
                        questions = questions,
                        currentQuestionIndex = session.currentQuestionIndex,
                        correctCount = session.correctCount,
                        wrongCount = session.wrongCount,
                        skippedCount = session.skippedCount,
                        isLoading = false,
                        isComplete = session.status == PracticeSessionStatus.COMPLETED
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Failed to load session", isLoading = false)
                }
            }
        }
    }

    /**
     * User answered the current question
     */
    private fun answerQuestion(answer: String) {
        val currentQuestion = _state.value.currentQuestion ?: return
        val isCorrect = answer == currentQuestion.correctAnswer

        // Calculate time spent
        val timeSpent = ((System.currentTimeMillis() - _state.value.questionStartTime) / 1000).toInt()

        // Update state to show answer result
        _state.update {
            it.copy(
                interactionState = QuestionInteractionState.Answered(
                    userAnswer = answer,
                    isCorrect = isCorrect,
                    explanation = currentQuestion.explanation
                )
            )
        }

        // Record answer in database
        viewModelScope.launch {
            try {
                // 1. Record in practice session
                practiceRepository.recordAnswer(
                    sessionId = sessionId,
                    questionId = currentQuestion.id,
                    answer = answer,
                    isCorrect = isCorrect,
                    timeSeconds = timeSpent
                )

                // 2. Update question stats (global performance tracking)
                updateQuestionStats(
                    questionId = currentQuestion.id,
                    isCorrect = isCorrect,
                    timeSeconds = timeSpent
                )

                // 3. Update streak (questions answered + time spent)
                streakRepository.recordQuestionsAnswered(1)
                streakRepository.recordTimeSpent(timeSpent.toLong())

                // Update local stats
                _state.update {
                    it.copy(
                        correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount,
                        wrongCount = if (!isCorrect) it.wrongCount + 1 else it.wrongCount
                    )
                }
            } catch (e: Exception) {
                // Log error but don't block UI
                println("Failed to record answer: ${e.message}")
            }
        }
    }

    /**
     * Continue to next question
     */
    private fun continueToNext() {
        val nextIndex = _state.value.currentQuestionIndex + 1

        if (nextIndex >= _state.value.questions.size) {
            // Session complete
            completeSession()
        } else {
            // Move to next question
            _state.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    interactionState = QuestionInteractionState.Idle,
                    questionStartTime = System.currentTimeMillis()
                )
            }
        }
    }

    /**
     * Skip current question
     */
    private fun skipQuestion() {
        val currentQuestion = _state.value.currentQuestion ?: return

        viewModelScope.launch {
            try {
                practiceRepository.skipQuestion(sessionId, currentQuestion.id)

                _state.update {
                    it.copy(skippedCount = it.skippedCount + 1)
                }

                continueToNext()
            } catch (e: Exception) {
                println("Failed to skip question: ${e.message}")
            }
        }
    }

    /**
     * Complete the session
     */
    private fun completeSession() {
        viewModelScope.launch {
            try {
                practiceRepository.completeSession(sessionId)
                awardXpUseCase(XpSource.PRACTICE_COMPLETE, sessionId.toString())

                _state.update {
                    it.copy(isComplete = true)
                }
            } catch (e: Exception) {
                println("Failed to complete session: ${e.message}")
            }
        }
    }

    /**
     * Exit session — marks it as abandoned so it won't resurface as "in progress".
     */
    private fun exitSession() {
        viewModelScope.launch {
            try {
                practiceRepository.updateSessionStatus(
                    sessionId = sessionId,
                    status = PracticeSessionStatus.ABANDONED
                )
            } catch (e: Exception) {
                // Best-effort; navigation proceeds regardless
                println("Failed to mark session abandoned: ${e.message}")
            }
        }
    }

    /**
     * Creates a new session with the same parameters and navigates to it.
     */
    private fun retrySession() {
        viewModelScope.launch {
            val session = _state.value.session ?: return@launch

            try {
                val filterUnitIds = session.filterUnitIds
                    ?.split(",")?.filter { it.isNotBlank() }
                val filterConceptIds = session.filterConceptIds
                    ?.split(",")?.filter { it.isNotBlank() }
                val filterQuestionTypes = session.filterQuestionTypes
                    ?.split(",")
                    ?.mapNotNull {
                        try { QuestionType.valueOf(it.trim()) } catch (e: Exception) { null }
                    }

                val newSessionId = practiceRepository.createPracticeSession(
                    subjectId = session.subjectId,
                    generationType = session.generationType,
                    questionCount = session.questionCount,
                    filterUnitIds = filterUnitIds,
                    filterConceptIds = filterConceptIds,
                    filterQuestionTypes = filterQuestionTypes
                )

                _navigationEvent.emit(PracticeNavEvent.NavigateToSession(newSessionId))
            } catch (e: Exception) {
                println("Failed to create retry session: ${e.message}")
            }
        }
    }

    /**
     * Update global question stats for performance tracking.
     * This data powers:
     * - Success rate per question
     * - Average time per question
     * - Weak areas detection
     * - Adaptive question selection
     */
    private suspend fun updateQuestionStats(
        questionId: String,
        isCorrect: Boolean,
        timeSeconds: Int
    ) {
        try {
            // Get existing stats or create new
            val stats = quizBankRepository.getStatsForQuestion(questionId)
                ?: QuestionStats.forNewQuestion(questionId)

            // Update with new response
            val updatedStats = stats.withNewResponse(isCorrect, timeSeconds)

            // Save back to database
            quizBankRepository.updateStats(updatedStats)
        } catch (e: Exception) {
            println("Failed to update question stats: ${e.message}")
        }
    }
}