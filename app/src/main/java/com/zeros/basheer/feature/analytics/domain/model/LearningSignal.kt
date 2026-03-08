package com.zeros.basheer.feature.analytics.domain.model

/**
 * The complete catalog of per-answer learning signals in Basheer.
 *
 * Previously named BasheerError — renamed because these records track ALL
 * answers (correct and wrong), not just failures. A student answering correctly
 * is still a meaningful learning signal for the SR algorithm.
 *
 * Design mirrors [BasheerEvent]:
 *  - Each subclass carries only the data it needs — no nullable bags.
 *  - Names describe the surface + what was attempted ("CheckpointAttempted", etc.)
 *  - Every signal is self-describing: reading it tells you exactly where the
 *    student was, what they answered, and what was correct.
 *  - No string keys — the type IS the contract.
 *
 * These records are the primary input for:
 *   - Per-question weak-spot analysis (which questions are hardest?)
 *   - Per-concept difficulty surfacing (which concepts need review?)
 *   - Recommendation engine feed (surface what students got wrong)
 *   - Practice suggestion filters (prioritise wrong/skipped questions)
 *   - Exam score breakdown (section-level and cognitive-level breakdowns)
 *
 * Storage: piped through [LearningSignalTracker] → [AnalyticsRepository.enqueueSignal]
 * → same Room table and Firestore upload path as events. Zero extra infra.
 */
sealed class LearningSignal {

    // ─────────────────────────────────────────────────────────────────────────
    // Lesson Checkpoints
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fired when a student submits a checkpoint answer (correct or wrong).
     * Checkpoints are soft-gates in the lesson reader — the student always
     * proceeds, but we record every outcome for weak-spot analysis.
     *
     * @param isCorrect  True = correct first try. False = wrong. We record
     *                   both so we can compute per-question accuracy rates.
     */
    data class CheckpointAttempted(
        val questionId: String,
        val lessonId: String,
        val sectionId: String,
        val subjectId: String,
        val unitId: String,
        val partIndex: Int,             // Which lesson part (0-indexed)
        val questionType: String,       // QuestionType.name — MCQ | ORDER
        val userAnswer: String,         // Serialized answer (option text or CSV-ordered items)
        val correctAnswer: String,
        val isCorrect: Boolean,
        val timeSpentSeconds: Int,      // Seconds from card render to submit tap
    ) : LearningSignal()

    // ─────────────────────────────────────────────────────────────────────────
    // Feed Mini-games (Spaced Repetition)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fired when the student answers a quiz card in the knowledge feed.
     * Feed cards are MCQ or TRUE_FALSE only (see [QuestionType.isFeedEligible]).
     *
     * Distinct from [CheckpointAttempted] because:
     *  - Feed answers drive the spaced repetition algorithm (ease-factor update).
     *  - The concept/card identity matters more than lesson position here.
     */
    data class FeedQuestionAnswered(
        val questionId: String,
        val feedCardId: String,         // FeedItem.id — for SR algorithm lookup
        val subjectId: String,
        val conceptId: String?,         // If the feed card is concept-linked
        val questionType: String,       // QuestionType.name
        val userAnswer: String,
        val correctAnswer: String,
        val isCorrect: Boolean,
        val timeSpentSeconds: Int,
        val cardPositionInSession: Int, // 0-indexed position in today's feed deck
        val srIntervalDaysBefore: Int,  // SR interval before this answer
    ) : LearningSignal()

    // ─────────────────────────────────────────────────────────────────────────
    // Practice Sessions
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fired once per question in a practice session, regardless of correctness.
     * The full answering picture — correct + wrong + skipped — is needed so the
     * recommendation engine can distinguish "got it right" from "skipped it".
     *
     * @param wasSkipped    True if the student tapped Skip rather than answering.
     *                      When true, [userAnswer] is empty and [isCorrect] is false.
     */
    data class PracticeQuestionAnswered(
        val questionId: String,
        val sessionId: Long,            // PracticeSession.id
        val subjectId: String,
        val unitId: String?,
        val lessonId: String?,
        val questionType: String,       // QuestionType.name
        val generationType: String,     // PracticeGenerationType.name — source of the session
        val userAnswer: String,         // Empty string if wasSkipped
        val correctAnswer: String,
        val isCorrect: Boolean,
        val wasSkipped: Boolean,
        val timeSpentSeconds: Int,
        val positionInSession: Int,     // 0-indexed question number within the session
        val attemptNumber: Int,         // Cumulative attempts on THIS question across all sessions
        val difficulty: Int,            // Question.difficulty — 1–5
        val cognitiveLevel: String,     // CognitiveLevel.name
    ) : LearningSignal()

    // ─────────────────────────────────────────────────────────────────────────
    // Exams
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fired once per question when an exam attempt is submitted.
     * Unlike practice, exam answers are only evaluated at submission time —
     * the student doesn't see feedback mid-session.
     *
     * @param wasUnanswered True if the question was left blank.
     * @param wasFlagged    Whether the student flagged this question for review
     *                      during the exam (flagged+wrong ≠ unflagged+wrong).
     */
    data class ExamQuestionEvaluated(
        val questionId: String,
        val attemptId: Long,            // QuizAttempt.id
        val examId: String,
        val subjectId: String,
        val examType: String,           // ExamType.name — MONTHLY | SEMI_FINAL | FINAL
        val sectionTitle: String?,      // Exam section label, e.g. "الفيزياء الحرارية"
        val questionType: String,       // QuestionType.name
        val userAnswer: String,
        val correctAnswer: String,
        val isCorrect: Boolean,
        val wasUnanswered: Boolean,
        val wasFlagged: Boolean,
        val positionInExam: Int,        // 0-indexed position
        val pointsAwarded: Int,         // Actual points given (0 or Question.points)
        val pointsAvailable: Int,       // Question.points
        val difficulty: Int,
        val cognitiveLevel: String,     // CognitiveLevel.name
        val source: String,             // QuestionSource.name — MINISTRY_FINAL | SCHOOL_EXAM | …
        val sourceYear: Int?,
    ) : LearningSignal()
}

// ─────────────────────────────────────────────────────────────────────────────
// Supporting enum — where did the signal surface?
// Used by the recommendation engine to weight signals differently.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Surfaces where a student can produce a learning signal, ordered roughly by
 * signal strength (exam signals are the highest-stakes).
 */
enum class SignalSurface {
    LESSON_CHECKPOINT,   // Inline gate inside a lesson part
    FEED_CARD,           // Spaced repetition mini-game in the feed
    PRACTICE_SESSION,    // Deliberate practice drill
    EXAM_SESSION,        // Timed exam attempt
}