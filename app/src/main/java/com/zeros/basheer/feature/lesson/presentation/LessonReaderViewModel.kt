package com.zeros.basheer.feature.lesson.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.core.domain.model.Result
import com.zeros.basheer.core.math.KatexRenderer
import com.zeros.basheer.domain.model.LessonContent
import com.zeros.basheer.domain.model.SectionUiModel
import com.zeros.basheer.feature.analytics.LearningSignalTracker
import com.zeros.basheer.feature.concept.domain.model.Concept
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
import com.zeros.basheer.feature.lesson.domain.repository.LessonRepository
import com.zeros.basheer.feature.lesson.domain.usecase.GetLessonContentUseCase
import com.zeros.basheer.feature.progress.data.entity.LessonPartProgressEntity
import com.zeros.basheer.feature.progress.data.dao.LessonPartProgressDao
import com.zeros.basheer.feature.progress.domain.model.UserProgress
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import com.zeros.basheer.feature.progress.domain.usecase.MarkLessonCompleteUseCase
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import com.zeros.basheer.feature.streak.domain.usecase.RecordLessonCompletedUseCase
import com.zeros.basheer.feature.streak.domain.usecase.RecordTimeSpentUseCase
import com.zeros.basheer.feature.user.domain.model.XpSource
import com.zeros.basheer.feature.analytics.AnalyticsManager
import com.zeros.basheer.feature.analytics.domain.model.LessonSource
import com.zeros.basheer.feature.streak.domain.usecase.CheckStreakMilestoneUseCase
import com.zeros.basheer.feature.user.domain.usecase.AwardXpAndCheckLevelUseCase
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

/**
 * Inline state for a single checkpoint gate within the lesson reader.
 *
 * @param question       The checkpoint question (MCQ or ORDER).
 * @param selected       The answer the user has tapped/arranged but not yet confirmed.
 * @param submitted      True once the user hits "تحقق" — reveals correct/wrong feedback.
 * @param isCorrect      Null until submitted.
 * @param renderTimeMs   Epoch millis when this checkpoint card became visible.
 *                       Used to compute [timeSpentSeconds] for error tracking.
 */
data class CheckpointUiState(
    val question: Question,
    val selected: String? = null,
    val submitted: Boolean = false,
    val isCorrect: Boolean? = null,
    val renderTimeMs: Long = System.currentTimeMillis(),  // ← NEW: for time tracking
)

data class LessonReaderState(
    // ── Content ───────────────────────────────────────────────────────────────
    val lessonContent: LessonContent? = null,
    /** Sections that belong to the current part only — derived from lessonContent. */
    val currentPartSections: List<SectionUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,

    // ── Part metadata ─────────────────────────────────────────────────────────
    /** 0-indexed part the user opened (from nav arg). */
    val currentPartIndex: Int = 0,
    /** Total number of distinct parts in this lesson (derived from sections). */
    val totalParts: Int = 1,
    /** Which parts have already been completed — drives top-bar tab states. */
    val completedPartIndices: Set<Int> = emptySet(),

    // ── Concept modal ─────────────────────────────────────────────────────────
    val activeConcept: Concept? = null,

    // ── Checkpoints for this part (keyed by sectionId) ────────────────────────
    val checkpoints: Map<String, CheckpointUiState> = emptyMap(),

    // ── Progress ──────────────────────────────────────────────────────────────
    val progress: UserProgress? = null,
    val readingTimeSeconds: Long = 0,
    /** True once user scrolls past the last block of the current part. */
    val hasScrolledToEnd: Boolean = false,

    // ── Completion modal ──────────────────────────────────────────────────────
    val showCompletionModal: Boolean = false,
    val xpEarned: Int = 0,
    val isRepeatCompletion: Boolean = false,
    val checkpointScore: Pair<Int, Int>? = null,     // correct / total

    // ── Forward pull ──────────────────────────────────────────────────────────
    /** Populated from LessonMetadata.forwardPull — shown on finish bar and exit dialog. */
    val forwardPull: String? = null,
    val nextLessonTitle: String? = null,

    // ── Part-complete flow ────────────────────────────────────────────────────
    /** True when this part is already marked complete (re-read). */
    val isPartAlreadyComplete: Boolean = false,
    /** True when the LESSON (all parts) is complete. */
    val isLessonComplete: Boolean = false,

    // ── Intro gate ────────────────────────────────────────────────────────────
    /**
     * True while the full-screen hook/orientation intro is visible.
     * Only shown for part 0; automatically false for subsequent parts.
     * Dismissed by the user tapping "ابدأ الدرس".
     */
    val showIntroCard: Boolean = false,
)

@HiltViewModel
class LessonReaderViewModel @Inject constructor(
    private val repository: LessonRepository,
    private val getLessonContentUseCase: GetLessonContentUseCase,
    private val markLessonCompleteUseCase: MarkLessonCompleteUseCase,
    private val progressRepository: ProgressRepository,
    private val partProgressDao: LessonPartProgressDao,
    private val conceptRepository: ConceptRepository,
    private val quizBankRepository: QuizBankRepository,
    private val recordLessonCompletedUseCase: RecordLessonCompletedUseCase,
    private val awardXpUseCase: AwardXpAndCheckLevelUseCase,
    private val checkStreakMilestoneUseCase: CheckStreakMilestoneUseCase,
    private val recordTimeSpentUseCase: RecordTimeSpentUseCase,
    private val errorTracker: LearningSignalTracker,
    private val analyticsManager: AnalyticsManager,
    val katexRenderer: KatexRenderer,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])
    private val initialPartIndex: Int = savedStateHandle["partIndex"] ?: 0

    private val _state = MutableStateFlow(LessonReaderState(currentPartIndex = initialPartIndex))
    val state: StateFlow<LessonReaderState> = _state.asStateFlow()

    private var timeTrackingJob: Job? = null
    private var isTrackingTime = false

    init {
        loadLesson()
        loadProgress()
        loadPartCompletion()
    }

    // ── Loading ───────────────────────────────────────────────────────────────

    private fun loadLesson() {
        viewModelScope.launch {
            when (val result = getLessonContentUseCase(lessonId)) {
                is Result.Success -> {
                    val content = result.data
                    val allParts = content.sections
                        .groupBy { it.partIndex }
                        .keys
                        .sorted()
                    val totalParts = allParts.size.coerceAtLeast(1)

                    val partSections = content.sections
                        .filter { it.partIndex == initialPartIndex }
                        .sortedBy { it.order }

                    _state.update {
                        it.copy(
                            lessonContent = content,
                            currentPartSections = partSections,
                            totalParts = totalParts,
                            forwardPull = content.metadata?.forwardPull,
                            showIntroCard = initialPartIndex == 0 &&
                                    (content.metadata?.hook != null ||
                                            content.metadata?.orientation?.isNotEmpty() == true),
                            isLoading = false
                        )
                    }
                    // Derive subjectId from unitId using deterministic ID convention
                    // (e.g. unitId "PHYSICS_U3" → subjectId "PHYSICS").
                    val subjectId = content.unitId.substringBefore("_U", content.unitId)
                    analyticsManager.lessonViewed(
                        lessonId    = lessonId,
                        subjectId   = subjectId,
                        unitId      = content.unitId,
                        source      = LessonSource.MAIN_SCREEN,
                        wasCompleted = _state.value.isLessonComplete,
                    )

                    loadCheckpoints()
                    loadNextLesson()
                }
                is Result.Error -> {
                    _state.update { it.copy(error = result.message, isLoading = false) }
                }
            }
        }
    }

    private fun loadNextLesson() {
        viewModelScope.launch {
            val next = repository.getNextLesson(lessonId)
            _state.update { it.copy(nextLessonTitle = next?.title) }
        }
    }

    private fun loadProgress() {
        viewModelScope.launch {
            progressRepository.getProgressByLesson(lessonId).collect { progress ->
                _state.update { it.copy(
                    progress = progress,
                    isLessonComplete = progress?.completed == true
                )}
            }
        }
    }

    private fun loadPartCompletion() {
        viewModelScope.launch {
            partProgressDao.getCompletedPartsForLessonFlow(lessonId).collect { completedParts ->
                val completedIndices = completedParts.map { it.partIndex }.toSet()
                _state.update {
                    it.copy(
                        completedPartIndices = completedIndices,
                        isPartAlreadyComplete = completedIndices.contains(initialPartIndex)
                    )
                }
            }
        }
    }

    private fun loadCheckpoints() {
        viewModelScope.launch {
            val checkpointMap = quizBankRepository.getCheckpointsForPart(lessonId, initialPartIndex)
            _state.update {
                it.copy(checkpoints = checkpointMap.mapValues { (_, q) ->
                    CheckpointUiState(question = q, renderTimeMs = System.currentTimeMillis())
                })
            }
        }
    }

    // ── Time tracking ─────────────────────────────────────────────────────────

    fun startTimeTracking() {
        if (isTrackingTime) return
        isTrackingTime = true
        updateLastAccessed()
        timeTrackingJob = viewModelScope.launch {
            while (isActive && isTrackingTime) {
                delay(1000)
                _state.update { it.copy(readingTimeSeconds = it.readingTimeSeconds + 1) }
            }
        }
    }

    fun pauseTimeTracking(saveTime: Boolean = true) {
        isTrackingTime = false
        timeTrackingJob?.cancel()
        timeTrackingJob = null
        if (saveTime) {
            viewModelScope.launch { saveReadingTime() }
        }
    }

    private fun updateLastAccessed() {
        viewModelScope.launch {
            val current = _state.value.progress
            progressRepository.updateProgress(
                (current ?: UserProgress(lessonId = lessonId))
                    .copy(lastAccessedAt = System.currentTimeMillis())
            )
        }
    }

    private suspend fun saveReadingTime() {
        val timeSpent = _state.value.readingTimeSeconds
        if (timeSpent > 0) {
            val current = _state.value.progress ?: UserProgress(lessonId = lessonId)
            progressRepository.updateProgress(
                current.copy(timeSpentSeconds = current.timeSpentSeconds + timeSpent.toInt())
            )
            recordTimeSpentUseCase(timeSpent)
        }
    }

    // ── Scroll tracking ───────────────────────────────────────────────────────

    fun onReachedEnd() {
        if (!_state.value.hasScrolledToEnd) {
            _state.update { it.copy(hasScrolledToEnd = true) }
        }
    }

    fun onScrolledAwayFromEnd() {
        if (!_state.value.showCompletionModal) {
            _state.update { it.copy(hasScrolledToEnd = false) }
        }
    }

    // ── Intro gate ────────────────────────────────────────────────────────────

    fun dismissIntro() {
        _state.update { it.copy(showIntroCard = false) }
    }

    // ── Concept modal ─────────────────────────────────────────────────────────

    fun onConceptClick(conceptId: String) {
        viewModelScope.launch {
            val concept = conceptRepository.getConceptById(conceptId)
            _state.update { it.copy(activeConcept = concept) }
        }
    }

    fun dismissConceptModal() {
        _state.update { it.copy(activeConcept = null) }
    }

    // ── Checkpoints ───────────────────────────────────────────────────────────

    fun onCheckpointSelect(sectionId: String, answer: String) {
        _state.update { state ->
            val updated = state.checkpoints.toMutableMap()
            updated[sectionId] = updated[sectionId]?.copy(selected = answer) ?: return
            state.copy(checkpoints = updated)
        }
    }

    fun onCheckpointSubmit(sectionId: String) {
        val cpState = _state.value.checkpoints[sectionId] ?: return
        val selected = cpState.selected ?: return

        val isCorrect = when (cpState.question.type) {
            QuestionType.MCQ   -> selected.trim() == cpState.question.correctAnswer.trim()
            QuestionType.ORDER -> selected.trim() == cpState.question.correctAnswer.trim()
            else               -> false
        }

        // ── Error tracking ────────────────────────────────────────────────────
        // Compute time from when the card rendered to when the student submitted.
        val timeSpentSeconds = ((System.currentTimeMillis() - cpState.renderTimeMs) / 1000)
            .toInt()
            .coerceAtLeast(0)

        val content = _state.value.lessonContent
        errorTracker.checkpointAttempted(
            questionId       = cpState.question.id,
            lessonId         = lessonId,
            sectionId        = sectionId,
            subjectId        = cpState.question.subjectId,
            unitId           = cpState.question.unitId ?: content?.unitId ?: "",
            partIndex        = initialPartIndex,
            questionType     = cpState.question.type.name,
            userAnswer       = selected,
            correctAnswer    = cpState.question.correctAnswer,
            isCorrect        = isCorrect,
            timeSpentSeconds = timeSpentSeconds,
        )
        // ─────────────────────────────────────────────────────────────────────

        _state.update { state ->
            val updated = state.checkpoints.toMutableMap()
            updated[sectionId] = cpState.copy(submitted = true, isCorrect = isCorrect)
            state.copy(checkpoints = updated)
        }
    }

    /** Soft gate — always lets through regardless of correctness. */
    fun onCheckpointContinue(sectionId: String) {
        // submitted+isCorrect state persists so UI stays in completed state
    }

    // ── Part / Lesson completion ───────────────────────────────────────────────

    fun markPartComplete() {
        pauseTimeTracking(saveTime = false)

        viewModelScope.launch {
            saveReadingTime()

            val timeSpent = _state.value.readingTimeSeconds.toInt()
            partProgressDao.markPartComplete(
                LessonPartProgressEntity.create(lessonId, initialPartIndex, timeSpent)
            )

            val completedParts = partProgressDao.getCompletedPartsForLesson(lessonId)
            val totalParts = _state.value.totalParts
            val allPartsComplete = completedParts.size >= totalParts
            val isLastPart = initialPartIndex >= totalParts - 1

            val isRepeat = _state.value.isLessonComplete
            var xpAwarded = 0

            when {
                allPartsComplete && !isRepeat -> {
                    markLessonCompleteUseCase(lessonId)
                    recordLessonCompletedUseCase()
                    // Award XP — level-up notification fires inside if boundary crossed
                    val tx = awardXpUseCase(XpSource.LESSON_COMPLETE, lessonId)
                    xpAwarded = tx?.amount ?: 0
                    // Check streak milestone now that today's activity is recorded
                    checkStreakMilestoneUseCase()
                    // Analytics: fire lessonCompleted for the first-time finish
                    val content = _state.value.lessonContent
                    if (content != null) {
                        val subjectId = content.unitId.substringBefore("_U", content.unitId)
                        analyticsManager.lessonCompleted(
                            lessonId         = lessonId,
                            subjectId        = subjectId,
                            unitId           = content.unitId,
                            timeSpentSeconds = _state.value.readingTimeSeconds.toInt(),
                            isFirstCompletion = true,
                            sectionsCount    = content.sections.size,
                        )
                    }
                }
                allPartsComplete && isRepeat -> {
                    // Award XP — level-up notification fires inside if boundary crossed
                    val tx = awardXpUseCase(XpSource.LESSON_REPEAT, lessonId)
                    xpAwarded = tx?.amount ?: 0
                }
                !isLastPart -> {
                    val partRef = "${lessonId}_part_$initialPartIndex"
                    // Award XP — level-up notification fires inside if boundary crossed
                    val tx = awardXpUseCase(XpSource.LESSON_PART_COMPLETE, partRef)
                    xpAwarded = tx?.amount ?: 0
                }
            }

            val submitted = _state.value.checkpoints.values.filter { it.submitted }
            val checkpointScore = if (submitted.isNotEmpty()) {
                submitted.count { it.isCorrect == true } to submitted.size
            } else null

            _state.update {
                it.copy(
                    showCompletionModal = true,
                    xpEarned = xpAwarded,
                    isRepeatCompletion = isRepeat,
                    checkpointScore = checkpointScore,
                    isLessonComplete = allPartsComplete
                )
            }
        }
    }

    fun dismissCompletionModal() {
        _state.update { it.copy(showCompletionModal = false) }
    }

    // ── Exit / Abandonment ────────────────────────────────────────────────────

    /**
     * Call this from [LessonReaderScreen] when the user confirms exit via the
     * [ExitConfirmationDialog] WITHOUT having completed the lesson.
     *
     * Already-complete lessons are skipped — revisiting a done lesson and
     * leaving mid-way is not an abandonment signal worth tracking.
     *
     * Safe to call from a composable BackHandler or the dialog confirm action.
     * The analytics call is fire-and-forget so it won't block navigation.
     */
    fun onExit() {
        val s = _state.value
        val content = s.lessonContent ?: return  // lesson not loaded yet — nothing to record
        if (s.isLessonComplete) return           // revisit exit, not an abandonment

        val subjectId = content.unitId.substringBefore("_U", content.unitId)
        val progressPercent = if (s.totalParts > 0)
            (s.completedPartIndices.size * 100) / s.totalParts
        else 0

        analyticsManager.lessonAbandoned(
            lessonId             = lessonId,
            subjectId            = subjectId,
            unitId               = content.unitId,
            abandonedAtPartIndex = s.currentPartIndex,
            totalParts           = s.totalParts,
            progressPercent      = progressPercent,
            timeSpentSeconds     = s.readingTimeSeconds.toInt(),
            source               = LessonSource.MAIN_SCREEN,
        )
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        isTrackingTime = false
        timeTrackingJob?.cancel()
    }
}