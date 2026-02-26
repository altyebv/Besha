package com.zeros.basheer.feature.user.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import com.zeros.basheer.feature.user.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val isDarkMode: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val dailyGoalMinutes: Int = 30
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val scheduler: ReminderScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferences.isDarkMode(),
                preferences.isNotificationsEnabled(),
                preferences.getReminderHour(),
                preferences.getReminderMinute(),
                preferences.getDailyGoalMinutes()
            ) { darkMode, notifications, hour, minute, goal ->
                SettingsState(
                    isDarkMode = darkMode,
                    isNotificationsEnabled = notifications,
                    reminderHour = hour,
                    reminderMinute = minute,
                    dailyGoalMinutes = goal
                )
            }.collect { _state.value = it }
        }
    }

    // ── Dark mode ─────────────────────────────────────────────────────────────

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferences.setDarkMode(enabled) }
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    /**
     * Enables or disables the daily reminder.
     * Schedules or cancels the alarm immediately so the change takes effect
     * without requiring an app restart.
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotificationsEnabled(enabled)
            if (enabled) {
                scheduler.schedule(
                    hour   = _state.value.reminderHour,
                    minute = _state.value.reminderMinute
                )
            } else {
                scheduler.cancel()
            }
        }
    }

    /**
     * Updates the reminder time and immediately reschedules the alarm
     * so the new time takes effect without reopening the app.
     */
    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferences.setReminderHour(hour)
            preferences.setReminderMinute(minute)
            if (_state.value.isNotificationsEnabled) {
                scheduler.schedule(hour, minute)
            }
        }
    }

    // ── Study goal ────────────────────────────────────────────────────────────

    fun setDailyGoalMinutes(minutes: Int) {
        viewModelScope.launch { preferences.setDailyGoalMinutes(minutes) }
    }
}