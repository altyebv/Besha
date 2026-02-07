package com.zeros.basheer.domain.model

import com.zeros.basheer.data.models.Subject

/**
 * Recommendation types that can be shown to users
 */
sealed class Recommendation {
    data class ContinueLesson(
        val lessonId: String,
        val lessonTitle: String,
        val unitTitle: String,
        val progress: Float, // 0-1
        val estimatedMinutes: Int
    ) : Recommendation()

    data class ReviewWeakConcept(
        val conceptId: String,
        val conceptName: String,
        val successRate: Float,
        val timesAsked: Int
    ) : Recommendation()

    data class CompleteUnit(
        val unitId: String,
        val unitTitle: String,
        val lessonsCompleted: Int,
        val totalLessons: Int,
        val percentComplete: Float
    ) : Recommendation()

    data class StreakAtRisk(
        val streakDays: Int,
        val hoursUntilLoss: Int
    ) : Recommendation()

    data class QuickReview(
        val questionCount: Int,
        val estimatedMinutes: Int
    ) : Recommendation()

    data class StartNewUnit(
        val unitId: String,
        val unitTitle: String,
        val lessonCount: Int
    ) : Recommendation()
}

/**
 * Scored recommendation with subject context
 */
data class ScoredRecommendation(
    val subject: Subject,
    val recommendation: Recommendation,
    val score: Float, // 0-100
    val badge: RecommendationBadge,
    val reason: String // User-friendly explanation
)

/**
 * Visual badges for recommendations
 */
enum class RecommendationBadge(val emoji: String, val label: String) {
    URGENT("⚠️", "عاجل"),
    HOT_STREAK("🔥", "سلسلة نشطة"),
    WEAK_AREA("💪", "تحتاج مراجعة"),
    ALMOST_DONE("🎯", "قارب على الانتهاء"),
    NEW_CONTENT("✨", "محتوى جديد"),
    AT_RISK("⏰", "في خطر"),
    QUICK_WIN("⚡", "سريع")
}

/**
 * Subject-level data used for scoring
 */
data class SubjectContext(
    val subjectId: String,
    val subjectName: String,

    // Progress data
    val lessonsCompleted: Int,
    val totalLessons: Int,
    val percentComplete: Float,

    // Performance data
    val averageSuccessRate: Float?, // null if no quizzes taken
    val weakConceptCount: Int,
    val totalQuestionsAsked: Int,

    // Recency data
    val lastStudiedTimestamp: Long?, // null if never studied
    val studySessionsToday: Int,
    val studySessionsThisWeek: Int,

    // Upcoming events
    val upcomingExamDays: Int? // null if no exam
)