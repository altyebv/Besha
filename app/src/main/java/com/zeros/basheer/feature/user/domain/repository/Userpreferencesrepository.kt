package com.zeros.basheer.feature.user.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    // ── Onboarding ────────────────────────────────────────────────────────────

    /** Synchronous check — used at app startup to gate navigation. */
    fun hasCompletedOnboarding(): Boolean

    suspend fun setOnboardingComplete(complete: Boolean)

    fun hasAnalyticsConsent(): Boolean

    suspend fun setAnalyticsConsent(granted: Boolean)

    // ── Appearance ────────────────────────────────────────────────────────────

    fun isDarkMode(): Flow<Boolean>
    suspend fun setDarkMode(enabled: Boolean)

    // ── Notifications ─────────────────────────────────────────────────────────

    fun isNotificationsEnabled(): Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)

    /** Hour of day (0–23) for the daily reminder. Default: 20 (8 PM). */
    fun getReminderHour(): Flow<Int>
    suspend fun setReminderHour(hour: Int)

    fun getReminderMinute(): Flow<Int>
    suspend fun setReminderMinute(minute: Int)

    // ── Study Goals ───────────────────────────────────────────────────────────

    /** Daily study target in minutes. Default: 30. */
    fun getDailyGoalMinutes(): Flow<Int>
    suspend fun setDailyGoalMinutes(minutes: Int)
}