package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks user activity per day for streak calculation.
 * 
 * Streak Levels:
 * - FLAME 🔥: Deep work (completed lesson, finished exam)
 * - SPARK ✨: Light engagement (reviewed cards, answered questions)
 * - COLD 💨: No activity (breaks streak unless frozen)
 * 
 * A FLAME grows the streak. A SPARK maintains it. COLD breaks it.
 */
@Entity(tableName = "daily_activity")
data class DailyActivity(
    @PrimaryKey
    val date: String,  // Format: "2025-02-06" (LocalDate.toString())
    
    // Activity counts
    val lessonsCompleted: Int = 0,
    val feedCardsReviewed: Int = 0,
    val quizQuestionsAnswered: Int = 0,
    val examsCompleted: Int = 0,
    val timeSpentSeconds: Long = 0,
    
    // Computed streak level (cached for quick queries)
    val streakLevel: StreakLevel = StreakLevel.COLD,
    
    // Timestamps
    val firstActivityAt: Long? = null,
    val lastActivityAt: Long = System.currentTimeMillis()
)

/**
 * Streak intensity levels.
 */
enum class StreakLevel {
    COLD,   // 💨 No meaningful activity
    SPARK,  // ✨ Light engagement (maintains streak)
    FLAME   // 🔥 Deep work (grows streak)
}

/**
 * Thresholds for streak level calculation.
 * These can be tuned based on user feedback.
 */
object StreakThresholds {
    // FLAME requirements (any ONE of these)
    const val LESSONS_FOR_FLAME = 1
    const val EXAMS_FOR_FLAME = 1
    
    // SPARK requirements (any ONE of these)
    const val CARDS_FOR_SPARK = 10
    const val QUESTIONS_FOR_SPARK = 5
    const val TIME_SECONDS_FOR_SPARK = 300  // 5 minutes
}

/**
 * User's overall streak status.
 */
data class StreakStatus(
    val currentStreak: Int,
    val longestStreak: Int,
    val todayLevel: StreakLevel,
    val lastActiveDate: String?,
    val isAtRisk: Boolean  // True if no activity today and streak > 0
)
