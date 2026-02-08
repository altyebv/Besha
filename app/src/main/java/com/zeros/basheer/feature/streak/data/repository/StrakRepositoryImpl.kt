package com.zeros.basheer.feature.streak.data.repository


import com.zeros.basheer.feature.streak.data.dao.DailyActivityDao
import com.zeros.basheer.feature.streak.data.entity.DailyActivityEntity
import com.zeros.basheer.feature.streak.data.entity.StreakLevel
import com.zeros.basheer.feature.streak.domain.model.DailyActivity
import com.zeros.basheer.feature.streak.domain.model.StreakStatus
import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Implementation of StreakRepository.
 * Contains all streak calculation and daily activity tracking business logic.
 */
class StreakRepositoryImpl @Inject constructor(
    private val dailyActivityDao: DailyActivityDao
) : StreakRepository {

    companion object {
        private val DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE
    }

    // ==================== Date Helpers ====================

    override fun getTodayDate(): String = LocalDate.now().format(DATE_FORMAT)

    override fun getYesterdayDate(): String = LocalDate.now().minusDays(1).format(DATE_FORMAT)

    private fun parseDate(dateString: String): LocalDate =
        LocalDate.parse(dateString, DATE_FORMAT)

    // ==================== Activity Recording ====================

    override suspend fun recordLessonCompleted() {
        dailyActivityDao.recordLessonCompleted(getTodayDate())
    }

    override suspend fun recordCardsReviewed(count: Int) {
        dailyActivityDao.recordCardsReviewed(getTodayDate(), count)
    }

    override suspend fun recordQuestionsAnswered(count: Int) {
        dailyActivityDao.recordQuestionsAnswered(getTodayDate(), count)
    }

    override suspend fun recordExamCompleted() {
        dailyActivityDao.recordExamCompleted(getTodayDate())
    }

    override suspend fun recordTimeSpent(seconds: Long) {
        dailyActivityDao.recordTimeSpent(getTodayDate(), seconds)
    }

    // ==================== Queries ====================

    override fun getTodayActivityFlow(): Flow<DailyActivity?> =
        dailyActivityDao.getActivityForDateFlow(getTodayDate()).map { it?.toDomain() }

    override suspend fun getTodayActivity(): DailyActivity? =
        dailyActivityDao.getActivityForDate(getTodayDate())?.toDomain()

    override fun getRecentActivity(days: Int): Flow<List<DailyActivity>> =
        dailyActivityDao.getRecentActivity(days).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getStreakStatus(): StreakStatus {
        val recentActivity = dailyActivityDao.getRecentActivity(365).first()

        if (recentActivity.isEmpty()) {
            return StreakStatus(
                currentStreak = 0,
                longestStreak = 0,
                todayLevel = StreakLevel.COLD,
                lastActiveDate = null,
                isAtRisk = false
            )
        }

        val today = getTodayDate()
        val yesterday = getYesterdayDate()

        // Find today's activity
        val todayActivity = recentActivity.find { it.date == today }
        val todayLevel = todayActivity?.streakLevel ?: StreakLevel.COLD

        // Calculate current streak
        val currentStreak = calculateCurrentStreak(recentActivity, today)

        // Calculate longest streak ever
        val longestStreak = calculateLongestStreak(recentActivity)

        // Find last active date
        val lastActiveDay = recentActivity.firstOrNull { it.streakLevel != StreakLevel.COLD }

        // Determine if streak is at risk (has streak but no activity today)
        val isAtRisk = currentStreak > 0 &&
                todayLevel == StreakLevel.COLD &&
                lastActiveDay?.date == yesterday

        return StreakStatus(
            currentStreak = currentStreak,
            longestStreak = maxOf(longestStreak, currentStreak),
            todayLevel = todayLevel,
            lastActiveDate = lastActiveDay?.date,
            isAtRisk = isAtRisk
        )
    }

    override fun getStreakStatusFlow(): Flow<StreakStatus> {
        return dailyActivityDao.getRecentActivity(365).map { recentActivity ->
            if (recentActivity.isEmpty()) {
                return@map StreakStatus(
                    currentStreak = 0,
                    longestStreak = 0,
                    todayLevel = StreakLevel.COLD,
                    lastActiveDate = null,
                    isAtRisk = false
                )
            }

            val today = getTodayDate()
            val yesterday = getYesterdayDate()

            val todayActivity = recentActivity.find { it.date == today }
            val todayLevel = todayActivity?.streakLevel ?: StreakLevel.COLD

            val currentStreak = calculateCurrentStreak(recentActivity, today)
            val longestStreak = calculateLongestStreak(recentActivity)
            val lastActiveDay = recentActivity.firstOrNull { it.streakLevel != StreakLevel.COLD }

            val isAtRisk = currentStreak > 0 &&
                    todayLevel == StreakLevel.COLD &&
                    lastActiveDay?.date == yesterday

            StreakStatus(
                currentStreak = currentStreak,
                longestStreak = maxOf(longestStreak, currentStreak),
                todayLevel = todayLevel,
                lastActiveDate = lastActiveDay?.date,
                isAtRisk = isAtRisk
            )
        }
    }

    // ==================== Stats ====================

    override fun getTotalLessonsCompleted(): Flow<Int> =
        dailyActivityDao.getTotalLessonsCompleted().map { it ?: 0 }

    override fun getTotalCardsReviewed(): Flow<Int> =
        dailyActivityDao.getTotalCardsReviewed().map { it ?: 0 }

    override fun getTotalQuestionsAnswered(): Flow<Int> =
        dailyActivityDao.getTotalQuestionsAnswered().map { it ?: 0 }

    override fun getTotalTimeSpent(): Flow<Long> =
        dailyActivityDao.getTotalTimeSpent().map { it ?: 0L }

    // ==================== Streak Calculation ====================

    /**
     * Calculates current streak by counting consecutive active days
     * going backwards from today/yesterday.
     */
    private fun calculateCurrentStreak(activities: List<DailyActivityEntity>, today: String): Int {
        val activityMap = activities.associateBy { it.date }

        // Check if today is active
        val todayActivity = activityMap[today]
        val todayActive = todayActivity?.streakLevel?.let { it != StreakLevel.COLD } ?: false

        // If today is not active, check yesterday as starting point
        val startDate = if (todayActive) {
            parseDate(today)
        } else {
            val yesterday = parseDate(today).minusDays(1)
            val yesterdayActivity = activityMap[yesterday.format(DATE_FORMAT)]
            if (yesterdayActivity?.streakLevel?.let { it != StreakLevel.COLD } != true) {
                // No activity today or yesterday = no streak
                return 0
            }
            yesterday
        }

        // Count consecutive days backwards
        var streak = 0
        var currentDate = startDate

        while (true) {
            val dateStr = currentDate.format(DATE_FORMAT)
            val activity = activityMap[dateStr]

            when (activity?.streakLevel) {
                StreakLevel.FLAME, StreakLevel.SPARK -> {
                    // Both FLAME and SPARK count as active days
                    streak++
                }
                else -> {
                    // No activity or COLD breaks the streak
                    break
                }
            }

            currentDate = currentDate.minusDays(1)

            // Safety limit
            if (streak > 365) break
        }

        return streak
    }

    /**
     * Calculates the longest streak ever recorded.
     */
    private fun calculateLongestStreak(activities: List<DailyActivityEntity>): Int {
        if (activities.isEmpty()) return 0

        // Sort by date descending (most recent first)
        val sorted = activities.sortedByDescending { it.date }

        var longestStreak = 0
        var currentStreak = 0
        var previousDate: LocalDate? = null

        for (activity in sorted) {
            if (activity.streakLevel == StreakLevel.COLD) {
                // Reset streak on cold day
                longestStreak = maxOf(longestStreak, currentStreak)
                currentStreak = 0
                previousDate = null
                continue
            }

            val currentDate = parseDate(activity.date)

            if (previousDate == null) {
                // First active day found
                currentStreak = 1
            } else {
                // Check if consecutive (previous is next day since we're going backwards)
                val expectedPrevious = currentDate.plusDays(1)
                if (previousDate == expectedPrevious) {
                    currentStreak++
                } else {
                    // Gap in days
                    longestStreak = maxOf(longestStreak, currentStreak)
                    currentStreak = 1
                }
            }

            previousDate = currentDate
        }

        return maxOf(longestStreak, currentStreak)
    }

    // ==================== Mapper ====================

    /**
     * Maps entity to domain model.
     */
    private fun DailyActivityEntity.toDomain(): DailyActivity = DailyActivity(
        date = date,
        lessonsCompleted = lessonsCompleted,
        feedCardsReviewed = feedCardsReviewed,
        quizQuestionsAnswered = quizQuestionsAnswered,
        examsCompleted = examsCompleted,
        timeSpentSeconds = timeSpentSeconds,
        streakLevel = streakLevel,
        firstActivityAt = firstActivityAt,
        lastActivityAt = lastActivityAt
    )
}