package com.zeros.basheer.feature.analytics

import android.util.Log
import com.zeros.basheer.feature.analytics.domain.model.BasheerError
import com.zeros.basheer.feature.analytics.domain.model.BasheerEvent
import com.zeros.basheer.feature.analytics.domain.repository.AnalyticsRepository
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single error-recording call-site across all of Basheer.
 *
 * "Error" here means any wrong answer, skip, or unanswered question —
 * not a crash or exception. This is a learning signal, not a bug report.
 *
 * ── Why a separate tracker? ──────────────────────────────────────────────────
 * [AnalyticsManager] handles behavioural events (what the user did).
 * [ErrorTracker] handles learning-quality signals (what the student got wrong).
 * Keeping them separate means:
 *   - The recommendation engine, feed algo, and practice filters can query
 *     error records without combing through the full events table.
 *   - Error payloads carry richer correctness/answer fields that don't belong
 *     in the leaner behavioural event stream.
 *   - Both still share the same [AnalyticsRepository] queue → same Room table
 *     → same Firestore sync path. Zero extra infra cost.
 *
 * ── Usage (from a ViewModel) ─────────────────────────────────────────────────
 * ```kotlin
 * errorTracker.checkpointAttempted(
 *     questionId = question.id,
 *     lessonId   = lessonId,
 *     sectionId  = sectionId,
 *     subjectId  = question.subjectId,
 *     unitId     = question.unitId ?: "",
 *     partIndex  = partIndex,
 *     questionType = question.type.name,
 *     userAnswer   = selectedAnswer,
 *     correctAnswer = question.correctAnswer,
 *     isCorrect    = isCorrect,
 *     timeSpentSeconds = elapsed,
 * )
 * ```
 *
 * All calls are non-blocking — fire and forget into the Room queue.
 *
 * ── Adding a new surface ──────────────────────────────────────────────────────
 *  1. Add the data class to [BasheerError].
 *  2. Add its JSON branch to [AnalyticsRepositoryImpl.toJson].
 *  3. Add a convenience function here.
 *  Done — no other files need changing.
 */
@Singleton
class ErrorTracker @Inject constructor(
    private val repository: AnalyticsRepository,
    private val preferencesRepository: UserPreferencesRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ─────────────────────────────────────────────────────────────────────────
    // Lesson Checkpoints
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Call this from [LessonReaderViewModel.onCheckpointSubmit] immediately
     * after computing [isCorrect]. Record both correct and wrong — the ratio
     * is the signal, not just the failures.
     */
    fun checkpointAttempted(
        questionId: String,
        lessonId: String,
        sectionId: String,
        subjectId: String,
        unitId: String,
        partIndex: Int,
        questionType: String,
        userAnswer: String,
        correctAnswer: String,
        isCorrect: Boolean,
        timeSpentSeconds: Int,
    ) = record(
        BasheerError.CheckpointAttempted(
            questionId = questionId,
            lessonId = lessonId,
            sectionId = sectionId,
            subjectId = subjectId,
            unitId = unitId,
            partIndex = partIndex,
            questionType = questionType,
            userAnswer = userAnswer,
            correctAnswer = correctAnswer,
            isCorrect = isCorrect,
            timeSpentSeconds = timeSpentSeconds,
        )
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Feed Mini-games
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Call this from [FeedsViewModel] when the student submits an answer on
     * a quiz-type feed card. The [srIntervalDaysBefore] value should be read
     * from the FeedItem before updating the SR schedule, so we capture the
     * interval that was active when the student answered.
     */
    fun feedQuestionAnswered(
        questionId: String,
        feedCardId: String,
        subjectId: String,
        conceptId: String?,
        questionType: String,
        userAnswer: String,
        correctAnswer: String,
        isCorrect: Boolean,
        timeSpentSeconds: Int,
        cardPositionInSession: Int,
        srIntervalDaysBefore: Int,
    ) = record(
        BasheerError.FeedQuestionAnswered(
            questionId = questionId,
            feedCardId = feedCardId,
            subjectId = subjectId,
            conceptId = conceptId,
            questionType = questionType,
            userAnswer = userAnswer,
            correctAnswer = correctAnswer,
            isCorrect = isCorrect,
            timeSpentSeconds = timeSpentSeconds,
            cardPositionInSession = cardPositionInSession,
            srIntervalDaysBefore = srIntervalDaysBefore,
        )
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Practice Sessions
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Call once per question from [PracticeSessionViewModel] when the student
     * taps "Continue" after answering, or "Skip". Always fire — for both
     * correct and wrong answers. Skips are recorded with [wasSkipped]=true.
     *
     * The [attemptNumber] should come from [QuestionStats.totalAttempts] + 1,
     * so the first-ever attempt is 1, second is 2, etc.
     */
    fun practiceQuestionAnswered(
        questionId: String,
        sessionId: Long,
        subjectId: String,
        unitId: String?,
        lessonId: String?,
        questionType: String,
        generationType: String,
        userAnswer: String,
        correctAnswer: String,
        isCorrect: Boolean,
        wasSkipped: Boolean,
        timeSpentSeconds: Int,
        positionInSession: Int,
        attemptNumber: Int,
        difficulty: Int,
        cognitiveLevel: String,
    ) = record(
        BasheerError.PracticeQuestionAnswered(
            questionId = questionId,
            sessionId = sessionId,
            subjectId = subjectId,
            unitId = unitId,
            lessonId = lessonId,
            questionType = questionType,
            generationType = generationType,
            userAnswer = userAnswer,
            correctAnswer = correctAnswer,
            isCorrect = isCorrect,
            wasSkipped = wasSkipped,
            timeSpentSeconds = timeSpentSeconds,
            positionInSession = positionInSession,
            attemptNumber = attemptNumber,
            difficulty = difficulty,
            cognitiveLevel = cognitiveLevel,
        )
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Exams
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Call once per question from [ExamSessionViewModel.submitExam] after
     * evaluating the student's answers against the answer key. Iterate the
     * full question list and record each one — correct, wrong, and unanswered.
     *
     * This is the highest-signal error data in the app. Don't skip it.
     */
    fun examQuestionEvaluated(
        questionId: String,
        attemptId: Long,
        examId: String,
        subjectId: String,
        examType: String,
        sectionTitle: String?,
        questionType: String,
        userAnswer: String,
        correctAnswer: String,
        isCorrect: Boolean,
        wasUnanswered: Boolean,
        wasFlagged: Boolean,
        positionInExam: Int,
        pointsAwarded: Int,
        pointsAvailable: Int,
        difficulty: Int,
        cognitiveLevel: String,
        source: String,
        sourceYear: Int?,
    ) = record(
        BasheerError.ExamQuestionEvaluated(
            questionId = questionId,
            attemptId = attemptId,
            examId = examId,
            subjectId = subjectId,
            examType = examType,
            sectionTitle = sectionTitle,
            questionType = questionType,
            userAnswer = userAnswer,
            correctAnswer = correctAnswer,
            isCorrect = isCorrect,
            wasUnanswered = wasUnanswered,
            wasFlagged = wasFlagged,
            positionInExam = positionInExam,
            pointsAwarded = pointsAwarded,
            pointsAvailable = pointsAvailable,
            difficulty = difficulty,
            cognitiveLevel = cognitiveLevel,
            source = source,
            sourceYear = sourceYear,
        )
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Core dispatch
    // ─────────────────────────────────────────────────────────────────────────

    private fun record(error: BasheerError) {
        // Reuse the same consent gate as AnalyticsManager.
        // Error records are behavioural data — same consent tier applies.
        if (!preferencesRepository.getAnalyticsConsent().isEnabled) return
        scope.launch {
            runCatching { repository.enqueueError(error) }
                .onFailure { Log.w(TAG, "Failed to enqueue ${error::class.simpleName}", it) }
        }
    }

    companion object {
        private const val TAG = "ErrorTracker"
    }
}