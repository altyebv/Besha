package com.zeros.basheer.feature.streak.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.zeros.basheer.feature.streak.data.entity.DailyActivityEntity
import com.zeros.basheer.feature.streak.data.entity.StreakLevel
import com.zeros.basheer.feature.streak.data.entity.StreakThresholds
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyActivityDao {

    // ==================== Queries ====================

    @Query("SELECT * FROM daily_activity WHERE date = :date")
    suspend fun getActivityForDate(date: String): DailyActivityEntity?

    @Query("SELECT * FROM daily_activity WHERE date = :date")
    fun getActivityForDateFlow(date: String): Flow<DailyActivityEntity?>

    @Query("SELECT * FROM daily_activity ORDER BY date DESC LIMIT :limit")
    fun getRecentActivity(limit: Int = 30): Flow<List<DailyActivityEntity>>

    @Query("SELECT * FROM daily_activity ORDER BY date DESC")
    fun getAllActivity(): Flow<List<DailyActivityEntity>>

    @Query("SELECT * FROM daily_activity WHERE streakLevel != 'COLD' ORDER BY date DESC LIMIT 1")
    suspend fun getLastActiveDay(): DailyActivityEntity?

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

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertActivity(activity: DailyActivityEntity)

    @Update
    suspend fun updateActivity(activity: DailyActivityEntity)

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
                DailyActivityEntity(
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
                DailyActivityEntity(
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
                DailyActivityEntity(
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
                DailyActivityEntity(
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
                DailyActivityEntity(
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
 * MUST BE OUTSIDE THE INTERFACE.
 */
private fun DailyActivityEntity.withRecalculatedLevel(): DailyActivityEntity {
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