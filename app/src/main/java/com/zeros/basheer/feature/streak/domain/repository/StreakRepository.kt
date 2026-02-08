package com.zeros.basheer.feature.streak.domain.repository


import com.zeros.basheer.feature.streak.data.entity.DailyActivityEntity
import com.zeros.basheer.feature.streak.domain.model.DailyActivity
import com.zeros.basheer.feature.streak.domain.model.StreakStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for streak and daily activity management.
 * Follows clean architecture - domain layer doesn't know about Room.
 */
interface StreakRepository {

    // ==================== Activity Recording ====================

    suspend fun recordLessonCompleted()

    suspend fun recordCardsReviewed(count: Int = 1)

    suspend fun recordQuestionsAnswered(count: Int = 1)

    suspend fun recordExamCompleted()

    suspend fun recordTimeSpent(seconds: Long)

    // ==================== Queries ====================

    /**
     * Gets today's activity as a Flow.
     */
    fun getTodayActivityFlow(): Flow<DailyActivity?>

    /**
     * Gets today's activity once.
     */
    suspend fun getTodayActivity(): DailyActivity?

    /**
     * Gets recent activity for calendar display.
     */
    fun getRecentActivity(days: Int = 30): Flow<List<DailyActivity>>

    /**
     * Gets current streak status (current streak, longest, today's level, etc.)
     */
    suspend fun getStreakStatus(): StreakStatus

    /**
     * Gets streak status as a Flow for reactive UI.
     */
    fun getStreakStatusFlow(): Flow<StreakStatus>

    // ==================== Stats ====================

    fun getTotalLessonsCompleted(): Flow<Int>

    fun getTotalCardsReviewed(): Flow<Int>

    fun getTotalQuestionsAnswered(): Flow<Int>

    fun getTotalTimeSpent(): Flow<Long>

    // ==================== Utilities ====================

    fun getTodayDate(): String

    fun getYesterdayDate(): String
}