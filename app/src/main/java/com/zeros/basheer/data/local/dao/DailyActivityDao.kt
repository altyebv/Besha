package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.DailyActivity
import com.zeros.basheer.data.models.StreakLevel
import com.zeros.basheer.data.models.StreakThresholds
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyActivityDao {
    
    // ==================== Queries ====================
    
    @Query("SELECT * FROM daily_activity WHERE date = :date")
    suspend fun getActivityForDate(date: String): DailyActivity?
    
    @Query("SELECT * FROM daily_activity WHERE date = :date")
    fun getActivityForDateFlow(date: String): Flow<DailyActivity?>
    
    @Query("SELECT * FROM daily_activity ORDER BY date DESC LIMIT :limit")
    fun getRecentActivity(limit: Int = 30): Flow<List<DailyActivity>>
    
    @Query("SELECT * FROM daily_activity ORDER BY date DESC")
    fun getAllActivity(): Flow<List<DailyActivity>>
    
    @Query("SELECT * FROM daily_activity WHERE streakLevel != 'COLD' ORDER BY date DESC LIMIT 1")
    suspend fun getLastActiveDay(): DailyActivity?
    
    @Query("""
        SELECT COUNT(*) FROM daily_activity 
        WHERE streakLevel != 'COLD' 
        AND date >= :startDate
    """)
    suspend fun getActiveDaysCount(startDate: String): Int
    
    @Query("SELECT SUM(lessonsCompleted) FROM daily_activity")
    fun getTotalLessonsCompleted(): Flow<Int?>
    
    @Query("SELECT SUM(feedCardsReviewed) FROM daily_activity")
    fun getTotalCardsReviewed(): Flow<Int?>
    
    @Query("SELECT SUM(quizQuestionsAnswered) FROM daily_activity")
    fun getTotalQuestionsAnswered(): Flow<Int?>
    
    @Query("SELECT SUM(timeSpentSeconds) FROM daily_activity")
    fun getTotalTimeSpent(): Flow<Long?>
    
    // ==================== Inserts/Updates ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: DailyActivity)
    
    @Update
    suspend fun updateActivity(activity: DailyActivity)
    
    @Query("DELETE FROM daily_activity")
    suspend fun deleteAllActivity()
    
    // ==================== Activity Recording ====================
    
    /**
     * Records a completed lesson for today.
     */
    @Transaction
    suspend fun recordLessonCompleted(date: String) {
        val existing = getActivityForDate(date)
        val now = System.currentTimeMillis()
        
        if (existing != null) {
            val updated = existing.copy(
                lessonsCompleted = existing.lessonsCompleted + 1,
                lastActivityAt = now
            )
            insertActivity(updated.withRecalculatedLevel())
        } else {
            insertActivity(
                DailyActivity(
                    date = date,
                    lessonsCompleted = 1,
                    firstActivityAt = now,
                    lastActivityAt = now
                ).withRecalculatedLevel()
            )
        }
    }
    
    /**
     * Records feed cards reviewed for today.
     */
    @Transaction
    suspend fun recordCardsReviewed(date: String, count: Int = 1) {
        val existing = getActivityForDate(date)
        val now = System.currentTimeMillis()
        
        if (existing != null) {
            val updated = existing.copy(
                feedCardsReviewed = existing.feedCardsReviewed + count,
                lastActivityAt = now
            )
            insertActivity(updated.withRecalculatedLevel())
        } else {
            insertActivity(
                DailyActivity(
                    date = date,
                    feedCardsReviewed = count,
                    firstActivityAt = now,
                    lastActivityAt = now
                ).withRecalculatedLevel()
            )
        }
    }
    
    /**
     * Records quiz questions answered for today.
     */
    @Transaction
    suspend fun recordQuestionsAnswered(date: String, count: Int = 1) {
        val existing = getActivityForDate(date)
        val now = System.currentTimeMillis()
        
        if (existing != null) {
            val updated = existing.copy(
                quizQuestionsAnswered = existing.quizQuestionsAnswered + count,
                lastActivityAt = now
            )
            insertActivity(updated.withRecalculatedLevel())
        } else {
            insertActivity(
                DailyActivity(
                    date = date,
                    quizQuestionsAnswered = count,
                    firstActivityAt = now,
                    lastActivityAt = now
                ).withRecalculatedLevel()
            )
        }
    }
    
    /**
     * Records an exam completed for today.
     */
    @Transaction
    suspend fun recordExamCompleted(date: String) {
        val existing = getActivityForDate(date)
        val now = System.currentTimeMillis()
        
        if (existing != null) {
            val updated = existing.copy(
                examsCompleted = existing.examsCompleted + 1,
                lastActivityAt = now
            )
            insertActivity(updated.withRecalculatedLevel())
        } else {
            insertActivity(
                DailyActivity(
                    date = date,
                    examsCompleted = 1,
                    firstActivityAt = now,
                    lastActivityAt = now
                ).withRecalculatedLevel()
            )
        }
    }
    
    /**
     * Records time spent studying for today.
     */
    @Transaction
    suspend fun recordTimeSpent(date: String, seconds: Long) {
        val existing = getActivityForDate(date)
        val now = System.currentTimeMillis()
        
        if (existing != null) {
            val updated = existing.copy(
                timeSpentSeconds = existing.timeSpentSeconds + seconds,
                lastActivityAt = now
            )
            insertActivity(updated.withRecalculatedLevel())
        } else {
            insertActivity(
                DailyActivity(
                    date = date,
                    timeSpentSeconds = seconds,
                    firstActivityAt = now,
                    lastActivityAt = now
                ).withRecalculatedLevel()
            )
        }
    }
}

/**
 * Extension function to recalculate streak level based on activity.
 */
private fun DailyActivity.withRecalculatedLevel(): DailyActivity {
    val level = when {
        // FLAME: Deep work achieved
        lessonsCompleted >= StreakThresholds.LESSONS_FOR_FLAME -> StreakLevel.FLAME
        examsCompleted >= StreakThresholds.EXAMS_FOR_FLAME -> StreakLevel.FLAME
        
        // SPARK: Light engagement
        feedCardsReviewed >= StreakThresholds.CARDS_FOR_SPARK -> StreakLevel.SPARK
        quizQuestionsAnswered >= StreakThresholds.QUESTIONS_FOR_SPARK -> StreakLevel.SPARK
        timeSpentSeconds >= StreakThresholds.TIME_SECONDS_FOR_SPARK -> StreakLevel.SPARK
        
        // COLD: Not enough activity
        else -> StreakLevel.COLD
    }
    return copy(streakLevel = level)
}
