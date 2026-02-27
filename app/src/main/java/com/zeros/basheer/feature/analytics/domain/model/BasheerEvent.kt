package com.zeros.basheer.feature.analytics.domain.model

/**
 * The complete catalog of trackable events in Basheer.
 *
 * Design rules:
 *  - Each subclass carries ONLY the data it needs — no shared bags of nullables.
 *  - Names are actions in past tense (what happened), not screens (where we are).
 *  - Every event is self-describing: reading it without context should tell the full story.
 *  - No string keys — the type IS the contract.
 *
 * Firestore write cost: all events for a day are batched into ONE document write.
 */
sealed class BasheerEvent {

    // ─────────────────────────────────────────────────────────────────────────
    // Session / Retention
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fired once when the app becomes foreground.
     * Captures a lightweight context snapshot for cohort analysis.
     */
    data class AppSessionStarted(
        val streakDays: Int,
        val totalXp: Int,
        val level: Int,
        val studentPath: String,            // "SCIENCE" | "LITERARY"
        val daysSinceLastOpen: Int,         // 0 = same day, 1 = yesterday, etc.
        val appVersion: String,
    ) : BasheerEvent()

    /**
     * Fired when app goes to background or is killed.
     * Paired with AppSessionStarted via sessionId on the AnalyticsEventEntity.
     */
    data class AppSessionEnded(
        val durationSeconds: Int,
        val activitiesCount: Int,           // Total sub-events fired in this session
    ) : BasheerEvent()

    /**
     * Fired once per calendar day when the daily goal is first satisfied.
     */
    data class DailyGoalReached(
        val streakDays: Int,
        val streakLevel: String,            // "SPARK" | "FLAME"
        val minutesSpent: Int,
    ) : BasheerEvent()

    // ─────────────────────────────────────────────────────────────────────────
    // Onboarding
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fired exactly once at the end of the onboarding flow.
     */
    data class OnboardingCompleted(
        val studentPath: String,
        val hasSchoolName: Boolean,
        val hasTargetExamDate: Boolean,
        val durationSeconds: Int,           // Time from WelcomeStep to completion
    ) : BasheerEvent()

    // ─────────────────────────────────────────────────────────────────────────
    // Lessons
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fired when a lesson screen becomes visible (scroll start).
     * Distinguishes where the user came from for funnel analysis.
     */
    data class LessonViewed(
        val lessonId: String,
        val subjectId: String,
        val unitId: String,
        val source: LessonSource,
        val wasCompleted: Boolean,          // Were they revisiting?
    ) : BasheerEvent()

    /**
     * Fired when the user reaches the last section and confirms completion.
     */
    data class LessonCompleted(
        val lessonId: String,
        val subjectId: String,
        val unitId: String,
        val timeSpentSeconds: Int,
        val isFirstCompletion: Boolean,
        val sectionsCount: Int,
    ) : BasheerEvent()

    // ─────────────────────────────────────────────────────────────────────────
    // Practice / Quiz
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fired when a practice session is created and begins.
     */
    data class PracticeSessionStarted(
        val sessionId: Long,
        val subjectId: String,
        val generationType: String,         // PracticeGenerationType.name
        val questionCount: Int,
        val hasTimeLimit: Boolean,
    ) : BasheerEvent()

    /**
     * Fired when a practice session reaches COMPLETED status.
     * Core learning quality signal.
     */
    data class PracticeSessionCompleted(
        val sessionId: Long,
        val subjectId: String,
        val generationType: String,
        val questionCount: Int,
        val correctCount: Int,
        val wrongCount: Int,
        val skippedCount: Int,
        val score: Float,                   // 0.0–1.0
        val durationSeconds: Int,
        val wasAbandoned: Boolean = false,
    ) : BasheerEvent()

    /**
     * Per-question signal — the richest learning quality data point.
     * Kept minimal: we don't store the question text, only its identity + outcome.
     */
    data class QuestionAnswered(
        val questionId: String,
        val subjectId: String,
        val questionType: String,           // McqQuestion, TrueFalse, etc.
        val isCorrect: Boolean,
        val timeSpentSeconds: Int,
        val sessionId: Long,                // Links back to PracticeSession
        val attemptNumber: Int,             // 1 = first ever attempt on this question
    ) : BasheerEvent()

    // ─────────────────────────────────────────────────────────────────────────
    // Feed / Spaced Repetition
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fired when the user interacts with a feed card (swipe, answer, skip).
     */
    data class FeedCardInteracted(
        val cardId: String,
        val subjectId: String,
        val cardType: String,               // "DEFINITION" | "FLASHCARD" | "QUIZ" etc.
        val interaction: FeedInteraction,
        val wasCorrect: Boolean?,           // null if non-answerable card
    ) : BasheerEvent()

    // ─────────────────────────────────────────────────────────────────────────
    // Exams
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fired when a full exam is submitted.
     */
    data class ExamCompleted(
        val examId: String,
        val subjectId: String,
        val examType: String,               // "MONTHLY" | "SEMI_FINAL" | "FINAL"
        val totalQuestions: Int,
        val score: Float,                   // 0.0–1.0
        val durationSeconds: Int,
    ) : BasheerEvent()

    // ─────────────────────────────────────────────────────────────────────────
    // Gamification
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fired when the user crosses a level boundary.
     */
    data class XpLevelUp(
        val newLevel: Int,
        val totalXp: Int,
        val xpSource: String,               // XpSource.name that triggered the level-up
    ) : BasheerEvent()

    /**
     * Fired at streak milestones (3, 7, 14, 30, 60, 100 days).
     */
    data class StreakMilestone(
        val streakDays: Int,
        val streakLevel: String,            // "SPARK" | "FLAME"
    ) : BasheerEvent()
}

// ─────────────────────────────────────────────────────────────────────────────
// Supporting enums
// ─────────────────────────────────────────────────────────────────────────────

enum class LessonSource {
    MAIN_SCREEN,        // Tapped from subject/unit list
    FEED_CARD,          // Tapped from a feed card CTA
    SEARCH,             // Arrived via search
    RECOMMENDATION,     // Arrived via smart recommendation
    PROFILE_HISTORY,    // Re-opened from profile activity log
}

enum class FeedInteraction {
    REVIEWED,           // Swiped through / marked as seen
    ANSWERED_CORRECT,
    ANSWERED_WRONG,
    SKIPPED,
    BOOKMARKED,
}