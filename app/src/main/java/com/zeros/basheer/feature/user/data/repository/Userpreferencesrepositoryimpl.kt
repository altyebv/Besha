package com.zeros.basheer.feature.user.data.repository

import android.content.SharedPreferences
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val prefs: SharedPreferences
) : UserPreferencesRepository {

    companion object {
        private const val KEY_ONBOARDING_COMPLETE  = "pref_onboarding_complete"
        private const val KEY_DARK_MODE             = "pref_dark_mode"
        private const val KEY_NOTIFICATIONS_ENABLED = "pref_notifications_enabled"
        private const val KEY_REMINDER_HOUR         = "pref_reminder_hour"
        private const val KEY_REMINDER_MINUTE       = "pref_reminder_minute"
        private const val KEY_DAILY_GOAL_MINUTES    = "pref_daily_goal_minutes"

        private const val KEY_ANALYTICS_CONSENT = "pref_analytics_consent"

        // Defaults
        private const val DEFAULT_REMINDER_HOUR    = 20  // 8 PM
        private const val DEFAULT_REMINDER_MINUTE  = 0
        private const val DEFAULT_DAILY_GOAL       = 30  // minutes
    }

    // ── Onboarding ────────────────────────────────────────────────────────────

    override fun hasCompletedOnboarding(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    override suspend fun setOnboardingComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply()
    }

    override fun hasAnalyticsConsent(): Boolean =
        prefs.getBoolean(KEY_ANALYTICS_CONSENT, false)

    override suspend fun setAnalyticsConsent(granted: Boolean) {
        prefs.edit().putBoolean(KEY_ANALYTICS_CONSENT, granted).apply()
    }

    // ── Appearance ────────────────────────────────────────────────────────────

    override fun isDarkMode(): Flow<Boolean> =
        prefs.observeBoolean(KEY_DARK_MODE, false)

    override suspend fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    override fun isNotificationsEnabled(): Flow<Boolean> =
        prefs.observeBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    override fun getReminderHour(): Flow<Int> =
        prefs.observeInt(KEY_REMINDER_HOUR, DEFAULT_REMINDER_HOUR)

    override suspend fun setReminderHour(hour: Int) {
        prefs.edit().putInt(KEY_REMINDER_HOUR, hour).apply()
    }

    override fun getReminderMinute(): Flow<Int> =
        prefs.observeInt(KEY_REMINDER_MINUTE, DEFAULT_REMINDER_MINUTE)

    override suspend fun setReminderMinute(minute: Int) {
        prefs.edit().putInt(KEY_REMINDER_MINUTE, minute).apply()
    }

    // ── Study Goals ───────────────────────────────────────────────────────────

    override fun getDailyGoalMinutes(): Flow<Int> =
        prefs.observeInt(KEY_DAILY_GOAL_MINUTES, DEFAULT_DAILY_GOAL)

    override suspend fun setDailyGoalMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_DAILY_GOAL_MINUTES, minutes).apply()
    }

    // ── SharedPreferences → Flow helpers ─────────────────────────────────────

    private fun SharedPreferences.observeBoolean(key: String, default: Boolean): Flow<Boolean> =
        callbackFlow {
            trySend(getBoolean(key, default))
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
                if (changedKey == key) trySend(getBoolean(key, default))
            }
            registerOnSharedPreferenceChangeListener(listener)
            awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
        }.distinctUntilChanged()

    private fun SharedPreferences.observeInt(key: String, default: Int): Flow<Int> =
        callbackFlow {
            trySend(getInt(key, default))
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
                if (changedKey == key) trySend(getInt(key, default))
            }
            registerOnSharedPreferenceChangeListener(listener)
            awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
        }.distinctUntilChanged()
}