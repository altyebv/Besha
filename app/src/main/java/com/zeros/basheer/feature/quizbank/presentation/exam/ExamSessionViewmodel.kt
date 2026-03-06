package com.zeros.basheer.feature.quizbank.presentation.exam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.analytics.ErrorTracker
import com.zeros.basheer.feature.practice.presentation.components.QuestionInteractionState
import com.zeros.basheer.feature.quizbank.domain.model.*
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import com.zeros.basheer.feature.quizbank.domain.usecase.CompleteQuizAttemptUseCase
import com.zeros.basheer.feature.quizbank.domain.usecase.RecordQuestionResponseUseCase
import com.zeros.basheer.feature.quizbank.domain.usecase.StartQuizAttemptUseCase
import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import com.zeros.basheer.feature.user.domain.model.XpSource
import com.zeros.basheer.feature.user.domain.usecase.AwardXpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State for exam session
 */
data class ExamSessionState(
    val exam: Exam? = null,
    val sections: List<ExamSection> = emptyList(),
    val questions: List<Question> = emptyList(),
    val attemptId: Long? = null,

    // Navigation
    val currentQuestionIndex: Int = 0,
    val currentSectionIndex: Int = 0,

    // Timer
    val timeRemainingSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val showTimeWarning: Boolean = false,
    val timeWarningMinutes: Int = 0,

    // Answers
    val answers: Map<String, String> = emptyMap(),  // questionId -> userAnswer
    val flaggedQuestions: Set<String> = emptySet(),
    val interactionState: QuestionInteractionState = QuestionInteractionState.Idle,

    // UI state
    val isLoading: Boolean = true,
    val error: String? = null,
    val showNavigationSheet: Boolean = false,
    val showSubmitDialog: Boolean = false,
    val isComplete: Boolean = false,

    // Integrity
    val strictMode: Boolean = false,
    val violationCount: Int = 0,
    val isInGracePeriod: Boolean = false,
    val showViolationWarningDialog: Boolean = false,

    // Stats
    val questionStartTime: Long = System.currentTimeMillis()
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentQuestionIndex)

    val currentSection: ExamSection? get() = sections.getOrNull(currentSectionIndex)

    val progress: Float get() = if (questions.isNotEmpty()) {
        currentQuestionIndex.toFloat() / questions.size
    } else 0f

    val answeredCount: Int get() = answers.size

    val unansweredCount: Int get() = questions.size - answers.size

    val isLastQuestion: Boolean get() = currentQuestionIndex >= questions.lastIndex

    fun isQuestionAnswered(questionId: String): Boolean = answers.containsKey(questionId)

    fun isQuestionFlagged(questionId: String): Boolean = flaggedQuestions.contains(questionId)
}

/**
 * Events for exam session
 */
sealed class ExamSessionEvent {
    data class AnswerQuestion(val answer: String) : ExamSessionEvent()
    object ContinueToNext : ExamSessionEvent()
    object PreviousQuestion : ExamSessionEvent()
    data class NavigateToQuestion(val index: Int) : ExamSessionEvent()
    object ToggleFlagCurrentQuestion : ExamSessionEvent()
    object ToggleNavigationSheet : ExamSessionEvent()
    object ShowSubmitDialog : ExamSessionEvent()
    object CancelSubmit : ExamSessionEvent()
    object ConfirmSubmit : ExamSessionEvent()
    object ExamViolation : ExamSessionEvent()
    object ExamResumed : ExamSessionEvent()
    object DismissViolationWarning : ExamSessionEvent()
}

@HiltViewModel
class ExamSessionViewModel @Inject constructor(
    private val quizBankRepository: QuizBankRepository,
    private val startQuizAttemptUseCase: StartQuizAttemptUseCase,
    private val completeQuizAttemptUseCase: CompleteQuizAttemptUseCase,
    private val awardXpUseCase: AwardXpUseCase,
    private val recordQuestionResponseUseCase: RecordQuestionResponseUseCase,
    private val streakRepository: StreakRepository,
    private val errorTracker: ErrorTracker,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val examId: String = savedStateHandle.get<String>("examId")
        ?: throw IllegalArgumentException("Exam ID is required")

    private val strictMode: Boolean = savedStateHandle.get<Boolean>("strictMode") ?: false

    private val _state = MutableStateFlow(ExamSessionState())
    val state: StateFlow<ExamSessionState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadExam()
    }

    fun onEvent(event: ExamSessionEvent) {
        when (event) {
            is ExamSessionEvent.AnswerQuestion -> answerQuestion(event.answer)
            ExamSessionEvent.ContinueToNext -> continueToNext()
            ExamSessionEvent.PreviousQuestion -> previousQuestion()
            is ExamSessionEvent.NavigateToQuestion -> navigateToQuestion(event.index)
            ExamSessionEvent.ToggleFlagCurrentQuestion -> toggleFlag()
            ExamSessionEvent.ToggleNavigationSheet -> toggleNavigationSheet()
            ExamSessionEvent.ShowSubmitDialog -> showSubmitDialog()
            ExamSessionEvent.CancelSubmit -> cancelSubmit()
            ExamSessionEvent.ConfirmSubmit -> submitExam()
            ExamSessionEvent.ExamViolation -> handleViolation()
            ExamSessionEvent.ExamResumed -> handleResume()
            ExamSessionEvent.DismissViolationWarning -> _state.update { it.copy(showViolationWarningDialog = false) }
        }
    }

    private fun loadExam() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                // Load exam
                val exam = quizBankRepository.getExamById(examId)
                    ?: throw IllegalStateException("Exam not found: $examId")
                val sections = exam.getSections()
                android.util.Log.d("ExamSession", "Exam loaded: ${exam.id}, sectionsJson=${exam.sectionsJson?.take(100)}")
                android.util.Log.d("ExamSession", "Sections parsed: ${sections.size} sections, IDs: ${sections.map { it.questionIds }}")

                // Load questions - prefer direct ID lookup from sections
                val questions = if (sections.isNotEmpty()) {
                    val questionIds = sections.flatMap { it.questionIds }
                    android.util.Log.d("ExamSession", "Loading ${questionIds.size} question IDs from sections: $questionIds")
                    val loaded = questionIds.mapNotNull { id -> quizBankRepository.getQuestionById(id) }
                    android.util.Log.d("ExamSession", "Loaded ${loaded.size}/${questionIds.size} questions by ID")
                    if (loaded.isEmpty() && questionIds.isNotEmpty()) {
                        android.util.Log.w("ExamSession", "⚠️ All IDs returned null — checking junction table fallback")
                        val fallback = quizBankRepository.getQuestionsForExam(examId)
                        android.util.Log.d("ExamSession", "Junction table returned ${fallback.size} questions")
                        fallback
                    } else {
                        loaded
                    }
                } else {
                    android.util.Log.d("ExamSession", "No sections — using junction table for exam $examId")
                    val fallback = quizBankRepository.getQuestionsForExam(examId)
                    android.util.Log.d("ExamSession", "Junction table returned ${fallback.size} questions")
                    fallback
                }
                android.util.Log.d("ExamSession", "Final question count: ${questions.size}")

                // Start attempt
                val attemptId = startQuizAttemptUseCase(examId)

                // Setup timer
                val duration = exam.duration ?: 180  // Default 3 hours
                val timeSeconds = duration * 60

                _state.update {
                    it.copy(
                        exam = exam,
                        sections = sections,
                        questions = questions,
                        attemptId = attemptId,
                        timeRemainingSeconds = timeSeconds,
                        isTimerRunning = true,
                        isLoading = false,
                        strictMode = strictMode,
                        questionStartTime = System.currentTimeMillis()
                    )
                }

                startTimer()

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Failed to load exam",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timeRemainingSeconds > 0 && _state.value.isTimerRunning) {
                delay(1000)
                _state.update { state ->
                    val newTime = state.timeRemainingSeconds - 1

                    // Show warning at 30, 10, 5 minutes
                    val warningMinutes = when (newTime) {
                        1800 -> 30; 600 -> 10; 300 -> 5; else -> 0
                    }

                    state.copy(
                        timeRemainingSeconds = newTime,
                        showTimeWarning = warningMinutes > 0,
                        timeWarningMinutes = warningMinutes
                    )
                }
            }

            // Time expired
            if (_state.value.timeRemainingSeconds <= 0) {
                autoSubmitExam(ExamAttemptStatus.TIME_EXPIRED)
            }
        }
    }

    private fun answerQuestion(answer: String) {
        val currentQuestion = _state.value.currentQuestion ?: return
        val isCorrect = answer == currentQuestion.correctAnswer

        // Calculate time spent on this question
        val timeSpent = ((System.currentTimeMillis() - _state.value.questionStartTime) / 1000).toInt()

        // Update state to show answer result
        _state.update {
            it.copy(
                interactionState = QuestionInteractionState.Answered(
                    userAnswer = answer,
                    isCorrect = isCorrect,
                    explanation = currentQuestion.explanation
                ),
                answers = it.answers + (currentQuestion.id to answer)
            )
        }

        // Record answer in background
        viewModelScope.launch {
            try {
                val attemptId = _state.value.attemptId ?: return@launch

                // Record in attempt
                recordQuestionResponseUseCase(
                    attemptId = attemptId,
                    questionId = currentQuestion.id,
                    userAnswer = answer,
                    isCorrect = isCorrect,
                    pointsEarned = if (isCorrect) currentQuestion.points else 0,
                    timeSpentSeconds = timeSpent
                )

                // Update question stats
                updateQuestionStats(currentQuestion.id, isCorrect, timeSpent)

                // Update streak
                streakRepository.recordQuestionsAnswered(1)
                streakRepository.recordTimeSpent(timeSpent.toLong())

            } catch (e: Exception) {
                println("Failed to record answer: ${e.message}")
            }
        }
    }

    private suspend fun updateQuestionStats(
        questionId: String,
        isCorrect: Boolean,
        timeSeconds: Int
    ) {
        try {
            val stats = quizBankRepository.getStatsForQuestion(questionId)
                ?: QuestionStats.forNewQuestion(questionId)
            val updatedStats = stats.withNewResponse(isCorrect, timeSeconds)
            quizBankRepository.updateStats(updatedStats)
        } catch (e: Exception) {
            println("Failed to update question stats: ${e.message}")
        }
    }

    private fun continueToNext() {
        val nextIndex = _state.value.currentQuestionIndex + 1

        if (nextIndex < _state.value.questions.size) {
            val newSectionIndex = findSectionForQuestionIndex(nextIndex)

            _state.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    currentSectionIndex = newSectionIndex,
                    interactionState = QuestionInteractionState.Idle,
                    questionStartTime = System.currentTimeMillis()
                )
            }
        }
    }

    private fun previousQuestion() {
        if (_state.value.currentQuestionIndex > 0) {
            val prevIndex = _state.value.currentQuestionIndex - 1
            val newSectionIndex = findSectionForQuestionIndex(prevIndex)

            _state.update {
                it.copy(
                    currentQuestionIndex = prevIndex,
                    currentSectionIndex = newSectionIndex,
                    interactionState = QuestionInteractionState.Idle,
                    questionStartTime = System.currentTimeMillis()
                )
            }
        }
    }

    private fun navigateToQuestion(index: Int) {
        if (index in _state.value.questions.indices) {
            val newSectionIndex = findSectionForQuestionIndex(index)

            _state.update {
                it.copy(
                    currentQuestionIndex = index,
                    currentSectionIndex = newSectionIndex,
                    interactionState = QuestionInteractionState.Idle,
                    showNavigationSheet = false,
                    questionStartTime = System.currentTimeMillis()
                )
            }
        }
    }

    private fun findSectionForQuestionIndex(questionIndex: Int): Int {
        val question = _state.value.questions.getOrNull(questionIndex) ?: return 0
        return _state.value.sections.indexOfFirst { section ->
            section.questionIds.contains(question.id)
        }.coerceAtLeast(0)
    }

    private fun toggleFlag() {
        val currentQuestion = _state.value.currentQuestion ?: return

        _state.update {
            val newFlags = if (it.flaggedQuestions.contains(currentQuestion.id)) {
                it.flaggedQuestions - currentQuestion.id
            } else {
                it.flaggedQuestions + currentQuestion.id
            }
            it.copy(flaggedQuestions = newFlags)
        }
    }

    private fun toggleNavigationSheet() {
        _state.update { it.copy(showNavigationSheet = !it.showNavigationSheet) }
    }

    private fun showSubmitDialog() {
        _state.update { it.copy(showSubmitDialog = true) }
    }

    private fun cancelSubmit() {
        _state.update { it.copy(showSubmitDialog = false) }
    }

    private fun submitExam() {
        viewModelScope.launch {
            try {
                val state = _state.value
                val attemptId = state.attemptId ?: return@launch
                val exam = state.exam ?: return@launch
                val totalTime = (exam.duration ?: 180) * 60 - state.timeRemainingSeconds

                // Calculate score
                val score = calculateScore()
                val totalPoints = exam.totalPoints ?: 100

                // Complete attempt
                completeQuizAttemptUseCase(
                    attemptId = attemptId,
                    score = score,
                    totalPoints = totalPoints,
                    timeSpentSeconds = totalTime
                )

                // Record exam completion for streak
                streakRepository.recordExamCompleted()
                awardXpUseCase(XpSource.EXAM_COMPLETE, attemptId.toString())

                // ── Error tracking — record every question outcome ─────────────
                // We do this after completeQuizAttemptUseCase so the attempt is
                // already persisted before the error records reference it.
                recordExamQuestionOutcomes(state, attemptId, exam)
                // ──────────────────────────────────────────────────────────────

                // Stop timer
                timerJob?.cancel()

                _state.update {
                    it.copy(
                        isTimerRunning = false,
                        showSubmitDialog = false,
                        isComplete = true
                    )
                }

            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to submit exam: ${e.message}") }
            }
        }
    }

    private fun autoSubmitExam(status: ExamAttemptStatus) {
        viewModelScope.launch {
            val state = _state.value
            val score = calculateScore()
            val totalPoints = state.exam?.totalPoints ?: 100
            val totalTime = (state.exam?.duration ?: 180) * 60

            state.attemptId?.let { attemptId ->
                try {
                    completeQuizAttemptUseCase(
                        attemptId = attemptId,
                        score = score,
                        totalPoints = totalPoints,
                        timeSpentSeconds = totalTime,
                        status = status.name
                    )

                    // Error tracking fires for auto-submit too (TIME_EXPIRED, DISQUALIFIED)
                    state.exam?.let { exam ->
                        recordExamQuestionOutcomes(state, attemptId, exam)
                    }
                } catch (e: Exception) {
                    println("Failed to auto-submit: ${e.message}")
                }
            }

            timerJob?.cancel()
            gracePeriodJob?.cancel()
            _state.update { it.copy(isComplete = true, isTimerRunning = false) }
        }
    }

    /**
     * Iterates every question in the exam and records its outcome to [ErrorTracker].
     * Called from both [submitExam] and [autoSubmitExam] so no surface is missed.
     *
     * Questions with no answer in [state.answers] are recorded as [wasUnanswered]=true.
     * The section title is resolved by looking up which [ExamSection] owns each question id.
     */
    private fun recordExamQuestionOutcomes(
        state: ExamSessionState,
        attemptId: Long,
        exam: Exam,
    ) {
        state.questions.forEachIndexed { index, question ->
            val userAnswer   = state.answers[question.id] ?: ""
            val wasUnanswered = userAnswer.isBlank()
            val isCorrect    = !wasUnanswered && userAnswer.trim() == question.correctAnswer.trim()

            // Resolve the section title this question belongs to
            val sectionTitle = state.sections
                .firstOrNull { it.questionIds.contains(question.id) }
                ?.title

            errorTracker.examQuestionEvaluated(
                questionId      = question.id,
                attemptId       = attemptId,
                examId          = exam.id,
                subjectId       = question.subjectId,
                examType        = exam.examType!!.name,
                sectionTitle    = sectionTitle,
                questionType    = question.type.name,
                userAnswer      = userAnswer,
                correctAnswer   = question.correctAnswer,
                isCorrect       = isCorrect,
                wasUnanswered   = wasUnanswered,
                wasFlagged      = state.flaggedQuestions.contains(question.id),
                positionInExam  = index,
                pointsAwarded   = if (isCorrect) question.points else 0,
                pointsAvailable = question.points,
                difficulty      = question.difficulty,
                cognitiveLevel  = question.cognitiveLevel.name,
                source          = question.source.name,
                sourceYear      = question.sourceYear,
            )
        }
    }

    private var gracePeriodJob: Job? = null

    private fun handleViolation() {
        val state = _state.value
        if (state.isComplete || state.isLoading) return

        if (state.strictMode) {
            // Strict mode: instant disqualification
            autoSubmitExam(ExamAttemptStatus.DISQUALIFIED)
            return
        }

        // Lenient mode: grace period + warning system
        val newViolationCount = state.violationCount + 1

        if (newViolationCount >= 2) {
            // Second offense: disqualify
            autoSubmitExam(ExamAttemptStatus.DISQUALIFIED)
            return
        }

        // First offense: start 15-second grace period
        _state.update {
            it.copy(
                violationCount = newViolationCount,
                isInGracePeriod = true
            )
        }

        gracePeriodJob?.cancel()
        gracePeriodJob = viewModelScope.launch {
            delay(15_000)
            // Grace period expired without resume — disqualify
            if (_state.value.isInGracePeriod) {
                autoSubmitExam(ExamAttemptStatus.DISQUALIFIED)
            }
        }
    }

    private fun handleResume() {
        if (_state.value.isInGracePeriod) {
            gracePeriodJob?.cancel()
            _state.update {
                it.copy(
                    isInGracePeriod = false,
                    showViolationWarningDialog = true
                )
            }
        }
    }

    private fun calculateScore(): Int {
        var totalScore = 0
        _state.value.answers.forEach { (questionId, userAnswer) ->
            val question = _state.value.questions.find { it.id == questionId }
            if (question != null && userAnswer == question.correctAnswer) {
                totalScore += question.points
            }
        }
        return totalScore
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        gracePeriodJob?.cancel()
    }
}