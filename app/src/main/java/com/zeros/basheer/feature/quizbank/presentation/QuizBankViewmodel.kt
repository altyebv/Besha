package com.zeros.basheer.feature.quizbank.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.practice.domain.model.PracticeGenerationType
import com.zeros.basheer.feature.practice.domain.model.PracticeSession
import com.zeros.basheer.feature.practice.domain.repository.PracticeRepository
import com.zeros.basheer.feature.quizbank.domain.model.Exam
import com.zeros.basheer.feature.quizbank.domain.model.ExamSource
import com.zeros.basheer.feature.quizbank.domain.model.QuestionCounts
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import com.zeros.basheer.feature.practice.domain.usecase.GetWeakAreaQuestionsUseCase
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
    val weakAreaCount: Int = 0,
    val isWeakAreaLoading: Boolean = false,
    val error: String? = null,
    // examId → last attempt percentage (0–100). null = never attempted.
    val examScores: Map<String, Float> = emptyMap()
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
    /**
     * Launch a WEAK_AREAS session for the current subject.
     * Handled by [GetWeakAreaQuestionsUseCase] — bypasses the filter path.
     * Emits [NavigateToPractice] or sets [QuizBankState.error] if no weak questions found.
     */
    object StartWeakAreaSession : QuizBankEvent()
    /**
     * Navigate to the Practice Builder screen, pre-selecting a mode.
     * Used by QuickModeStrip chips that need filter context before creating a session.
     */
    data class OpenPracticeBuilder(val mode: PracticeGenerationType) : QuizBankEvent()
    object Refresh : QuizBankEvent()
}

@HiltViewModel
class QuizBankViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val quizBankRepository: QuizBankRepository,
    private val practiceRepository: PracticeRepository,
    private val getWeakAreaQuestions: GetWeakAreaQuestionsUseCase,
) : ViewModel() {

    sealed class NavigationEvent {
        data class NavigateToPractice(val sessionId: Long) : NavigationEvent()
        data class NavigateToExam(val examId: String) : NavigationEvent()
        data class NavigateToPracticeBuilder(val subjectId: String, val mode: String) : NavigationEvent()
    }

    private val _state = MutableStateFlow(QuizBankState())
    val state: StateFlow<QuizBankState> = _state.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    /**
     * Subject context for this screen instance.
     *
     * - Launched from a recommendation card → a concrete subjectId is passed as a nav arg,
     *   and the screen scopes itself to that subject.
     * - Launched from the bottom nav → no subjectId arg → null here → we load across
     *   ALL subjects so nothing is invisible just because there's no lesson file yet.
     */
    private val currentSubjectId: String?
        get() = savedStateHandle.get<String>("subjectId")
            ?.takeIf { it.isNotBlank() && it != "{subjectId}" } // guard against template literal

    /** True when the screen is not scoped to a specific subject. */
    private val isAllSubjects: Boolean
        get() = currentSubjectId == null

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
            QuizBankEvent.StartWeakAreaSession -> {
                startWeakAreaSession()
            }
            is QuizBankEvent.OpenPracticeBuilder -> {
                viewModelScope.launch {
                    _navigationEvent.emit(
                        NavigationEvent.NavigateToPracticeBuilder(
                            subjectId = currentSubjectId ?: "geography",
                            mode = event.mode.name
                        )
                    )
                }
            }
            QuizBankEvent.Refresh -> {
                loadData()
            }
        }
    }

    private fun loadData() {
        _state.update { it.copy(isLoading = true, error = null) }

        if (isAllSubjects) {
            // ── No subject scoping: load ALL exams regardless of subject ──────
            // Splits the flat list into ministry / school / custom buckets in-memory
            // so ExamsModeContent (and its filter chips) keep working unchanged.
            viewModelScope.launch {
                quizBankRepository.getAllExams()
                    .catch { e -> _state.update { it.copy(error = e.message) } }
                    .collect { all ->
                        _state.update {
                            it.copy(
                                ministryExams  = all.filter { e -> e.source == ExamSource.MINISTRY },
                                schoolExams    = all.filter { e -> e.source == ExamSource.SCHOOL },
                                // Bug fix: CUSTOM is the source used in Exams_.json, not PRACTICE
                                practiceExams  = all.filter { e ->
                                    e.source == ExamSource.CUSTOM || e.source == ExamSource.PRACTICE
                                }
                            )
                        }
                        loadExamScores(all.map { it.id })
                    }
            }
        } else {
            // ── Subject-scoped: three targeted flows (same as before) ─────────
            val subjectId = currentSubjectId!!

            viewModelScope.launch {
                quizBankRepository.getExamsBySource(subjectId, ExamSource.MINISTRY)
                    .catch { e -> _state.update { it.copy(error = e.message) } }
                    .collect { exams ->
                        _state.update { it.copy(ministryExams = exams) }
                        loadExamScores(exams.map { it.id })
                    }
            }

            viewModelScope.launch {
                quizBankRepository.getExamsBySource(subjectId, ExamSource.SCHOOL)
                    .catch { e -> _state.update { it.copy(error = e.message) } }
                    .collect { exams ->
                        _state.update { it.copy(schoolExams = exams) }
                        loadExamScores(exams.map { it.id })
                    }
            }

            // Bug fix: CUSTOM is the source used in Exams_.json, not PRACTICE
            viewModelScope.launch {
                combine(
                    quizBankRepository.getExamsBySource(subjectId, ExamSource.CUSTOM),
                    quizBankRepository.getExamsBySource(subjectId, ExamSource.PRACTICE)
                ) { custom, practice -> custom + practice }
                    .catch { e -> _state.update { it.copy(error = e.message) } }
                    .collect { exams -> _state.update { it.copy(practiceExams = exams) } }
            }
        }

        // Load recent practice sessions
        viewModelScope.launch {
            practiceRepository.getRecentCompletedSessions(10)
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { sessions -> _state.update { it.copy(recentSessions = sessions) } }
        }

        // Load active session
        viewModelScope.launch {
            val activeSession = practiceRepository.getActiveSession()
            _state.update { it.copy(activeSession = activeSession) }
        }

        // Question counts and stats are subject-scoped — only meaningful when a
        // subject is selected. Skip them in all-subjects mode to avoid misleading data.
        currentSubjectId?.let { subjectId ->
            viewModelScope.launch {
                val counts = quizBankRepository.getQuestionCounts(subjectId)
                _state.update { it.copy(questionCounts = counts) }
            }

            viewModelScope.launch {
                practiceRepository.getAverageScore(subjectId)
                    .catch { e -> _state.update { it.copy(error = e.message) } }
                    .collect { score -> _state.update { it.copy(averageScore = score, isLoading = false) } }
            }

            viewModelScope.launch {
                practiceRepository.getCompletedSessionCount(subjectId)
                    .catch { e -> _state.update { it.copy(error = e.message) } }
                    .collect { count -> _state.update { it.copy(completedSessionCount = count) } }
            }

            viewModelScope.launch {
                val weakStats = getWeakAreaQuestions.getWeakStats(subjectId)
                _state.update { it.copy(weakAreaCount = weakStats.size) }
            }
        } ?: _state.update { it.copy(isLoading = false) }
    }

    /**
     * Fetches the last completed attempt score for each exam in [examIds] and
     * merges the results into [QuizBankState.examScores].
     * Runs N small suspending queries (one per exam) — fine for a list of ≤30 exams.
     */
    private fun loadExamScores(examIds: List<String>) {
        if (examIds.isEmpty()) return
        viewModelScope.launch {
            val scores = examIds
                .mapNotNull { id ->
                    val attempt = quizBankRepository.getLastAttemptForExam(id)
                    attempt?.percentage?.let { pct -> id to pct }
                }
                .toMap()
            // Merge with any existing scores (e.g. ministry + school loaded separately)
            _state.update { it.copy(examScores = it.examScores + scores) }
        }
    }

    private fun startExam(examId: String) {
        viewModelScope.launch {
            try {
                // Emit navigation event to start exam
                _navigationEvent.emit(NavigationEvent.NavigateToExam(examId))
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
        val subjectId = currentSubjectId ?: "geography" // fallback only for session creation
        viewModelScope.launch {
            try {
                val sessionId = practiceRepository.createPracticeSession(
                    subjectId = subjectId,
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

    private fun startWeakAreaSession() {
        val subjectId = currentSubjectId ?: "geography"
        viewModelScope.launch {
            try {
                _state.update { it.copy(isWeakAreaLoading = true) }
                val sessionId = getWeakAreaQuestions(
                    subjectId = subjectId,
                    maxQuestions = 20,
                    shuffled = true,
                )
                if (sessionId != null) {
                    _navigationEvent.emit(NavigationEvent.NavigateToPractice(sessionId))
                } else {
                    _state.update { it.copy(error = "لا توجد أسئلة ضعيفة كافية بعد — أجب على المزيد من الأسئلة أولاً") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            } finally {
                _state.update { it.copy(isWeakAreaLoading = false) }
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