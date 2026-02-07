package com.zeros.basheer.ui.screens.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.data.models.*
import com.zeros.basheer.data.repository.QuizBankRepository
import com.zeros.basheer.domain.model.CardInteractionState
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
    val interactionState: CardInteractionState = CardInteractionState.Idle,
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

@HiltViewModel
class PracticeSessionViewModel @Inject constructor(
    private val quizBankRepository: QuizBankRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: Long = savedStateHandle.get<Long>("sessionId")
        ?: throw IllegalArgumentException("Session ID is required")

    private val _state = MutableStateFlow(PracticeSessionState())
    val state: StateFlow<PracticeSessionState> = _state.asStateFlow()

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
                val session = quizBankRepository.getSession(sessionId)
                if (session == null) {
                    _state.update { it.copy(error = "Session not found", isLoading = false) }
                    return@launch
                }

                val sessionQuestions = quizBankRepository.getSessionQuestions(sessionId)
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
                interactionState = CardInteractionState.Answered(
                    userAnswer = answer,
                    isCorrect = isCorrect,
                    explanation = currentQuestion.explanation
                )
            )
        }

        // Record answer in database
        viewModelScope.launch {
            try {
                quizBankRepository.recordPracticeAnswer(
                    sessionId = sessionId,
                    questionId = currentQuestion.id,
                    answer = answer,
                    isCorrect = isCorrect,
                    timeSeconds = timeSpent
                )

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
                    interactionState = CardInteractionState.Idle,
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
                quizBankRepository.skipQuestion(sessionId, currentQuestion.id)

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
                quizBankRepository.completePracticeSession(sessionId)

                _state.update {
                    it.copy(isComplete = true)
                }
            } catch (e: Exception) {
                println("Failed to complete session: ${e.message}")
            }
        }
    }

    /**
     * Exit session (abandon)
     */
    private fun exitSession() {
        // TODO: Mark session as abandoned and navigate away
        viewModelScope.launch {
            // Update session status to ABANDONED
            // Navigate back
        }
    }

    /**
     * Retry the same session type (create new session)
     */
    private fun retrySession() {
        // TODO: Create a new session with the same parameters
        viewModelScope.launch {
            val session = _state.value.session ?: return@launch

            // Parse filter parameters
            val filterUnitIds = session.filterUnitIds?.split(",")
            val filterConceptIds = session.filterConceptIds?.split(",")
            val filterQuestionTypes = session.filterQuestionTypes?.split(",")
                ?.mapNotNull {
                    try { QuestionType.valueOf(it) } catch (e: Exception) { null }
                }

            // Create new session
            val newSessionId = quizBankRepository.createPracticeSession(
                subjectId = session.subjectId,
                generationType = session.generationType,
                questionCount = session.questionCount,
                filterUnitIds = filterUnitIds,
                filterConceptIds = filterConceptIds,
                filterQuestionTypes = filterQuestionTypes
            )

            // TODO: Navigate to new session
        }
    }
}