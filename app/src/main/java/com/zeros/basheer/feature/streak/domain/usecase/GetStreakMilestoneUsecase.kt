package com.zeros.basheer.feature.streak.domain.usecase

import com.zeros.basheer.feature.analytics.AnalyticsManager
import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import javax.inject.Inject

/**
 * Checks whether the user's current streak just crossed a milestone after
 * an activity was recorded, and fires the streak-milestone achievement
 * notification if it did.
 *
 * Call this immediately after any [StreakRepository.recordX()] call that
 * could extend the streak — currently lesson completion and exam completion,
 * since those are the two actions that trigger daily streak progression.
 *
 * Feed card reviews and practice questions intentionally do NOT call this —
 * they record time/questions for the streak level calculation but a single
 * card swipe shouldn't be the moment we announce "7-day streak!". The lesson
 * or exam completion is the meaningful daily anchor.
 *
 * Milestone detection is edge-triggered, not level-triggered:
 *  - We read streak AFTER recording today's activity.
 *  - We only fire if today's streak value is exactly a milestone number.
 *  - "Exactly" avoids re-firing on subsequent actions the same day.
 *  - The AnalyticsManager.streakMilestone() call is idempotent for the
 *    same (streakDays, date) pair because the analytics event is just queued
 *    once — even if somehow called twice, it won't double-post the notification
 *    because NotificationManager.notify() with the same ID replaces the old one.
 */
class CheckStreakMilestoneUseCase @Inject constructor(
    private val streakRepository: StreakRepository,
    private val analyticsManager: AnalyticsManager,
) {
    suspend operator fun invoke() {
        val status = streakRepository.getStreakStatus()
        val streak = status.currentStreak

        // AnalyticsManager.streakMilestone() already guards the milestone set
        // internally — passing a non-milestone value is a no-op.
        if (streak > 0) {
            analyticsManager.streakMilestone(
                streakDays  = streak,
                streakLevel = status.todayLevel.name,
            )
        }
    }
}