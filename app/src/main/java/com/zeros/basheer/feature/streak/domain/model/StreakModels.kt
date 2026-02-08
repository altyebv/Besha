package com.zeros.basheer.feature.streak.domain.model


import com.zeros.basheer.feature.streak.data.entity.StreakLevel

/**
 * User's overall streak status.
 * This is a domain model (no Room annotations).
 */
data class StreakStatus(
    val currentStreak: Int,
    val longestStreak: Int,
    val todayLevel: StreakLevel,
    val lastActiveDate: String?,
    val isAtRisk: Boolean  // True if no activity today and streak > 0
)

/**
 * Domain model for daily activity (without Room annotations).
 * Used by UI layer.
 */
data class DailyActivity(
    val date: String,
    val lessonsCompleted: Int,
    val feedCardsReviewed: Int,
    val quizQuestionsAnswered: Int,
    val examsCompleted: Int,
    val timeSpentSeconds: Long,
    val streakLevel: StreakLevel,
    val firstActivityAt: Long?,
    val lastActivityAt: Long
)