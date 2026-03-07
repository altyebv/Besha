package com.zeros.basheer.feature.user.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and cancels the daily study reminder alarm AND the streak-risk check.
 *
 * ── Daily reminder ────────────────────────────────────────────────────────────
 * Fires [ReminderReceiver] at the user-configured hour:minute every day.
 * Uses [AlarmManager.setExactAndAllowWhileIdle] (API 23+) or falls back to
 * [AlarmManager.setWindow] on API 31+ when exact-alarm permission is missing.
 *
 * ── Streak-risk check ────────────────────────────────────────────────────────
 * Fires [ReminderReceiver] with action [ACTION_STREAK_RISK] two hours before
 * the user's reminder time (minimum 18:00). [ReminderReceiver] checks whether
 * the user has already studied today and only shows the high-importance alert
 * if their streak is at risk — no notification is shown otherwise.
 *
 * Both alarms are rescheduled together whenever settings change or on device boot.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    private val context: Context,
    private val preferences: UserPreferencesRepository
) {

    companion object {
        private const val REQUEST_CODE_REMINDER     = 2001
        private const val REQUEST_CODE_STREAK_RISK  = 2002

        /** Action string that distinguishes streak-risk alarm from the daily reminder. */
        const val ACTION_STREAK_RISK = "com.zeros.basheer.STREAK_RISK_CHECK"

        /** Earliest hour the streak-risk notification fires (6 PM). */
        private const val MIN_STREAK_RISK_HOUR = 18
    }

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Schedules both the daily reminder and the streak-risk check.
     * Cancels any existing alarms first to avoid duplicates.
     *
     * @param hour   User-configured reminder hour (0–23).
     * @param minute User-configured reminder minute (0–59).
     */
    fun schedule(hour: Int, minute: Int) {
        cancel()
        scheduleAlarm(
            requestCode = REQUEST_CODE_REMINDER,
            action      = null,                          // default alarm action
            triggerAt   = nextAlarmTime(hour, minute),
        )
        scheduleAlarm(
            requestCode = REQUEST_CODE_STREAK_RISK,
            action      = ACTION_STREAK_RISK,
            triggerAt   = nextAlarmTime(streakRiskHour(hour), minute),
        )
    }

    /** Cancels both alarms. */
    fun cancel() {
        alarmManager.cancel(buildPendingIntent(REQUEST_CODE_REMINDER, action = null))
        alarmManager.cancel(buildPendingIntent(REQUEST_CODE_STREAK_RISK, ACTION_STREAK_RISK))
    }

    /**
     * Called on [android.content.Intent.ACTION_BOOT_COMPLETED].
     * Reads prefs synchronously (fast SharedPreferences read) and reschedules
     * only if the user has notifications enabled.
     */
    fun rescheduleIfEnabled() {
        val (enabled, hour, minute) = runBlocking {
            Triple(
                preferences.isNotificationsEnabled().first(),
                preferences.getReminderHour().first(),
                preferences.getReminderMinute().first(),
            )
        }
        if (enabled) schedule(hour, minute) else cancel()
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private fun scheduleAlarm(requestCode: Int, action: String?, triggerAt: Long) {
        val intent = buildPendingIntent(requestCode, action)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
                } else {
                    alarmManager.setWindow(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        10 * 60 * 1000L,  // 10-min window — fine for a reminder
                        intent,
                    )
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
            else ->
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAt, AlarmManager.INTERVAL_DAY, intent)
        }
    }

    /**
     * Returns the epoch-ms for the next occurrence of [hour]:[minute].
     * If that time has already passed today, returns tomorrow's occurrence.
     */
    private fun nextAlarmTime(hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis

    /**
     * Returns the hour for the streak-risk alarm:
     * 2 hours before [reminderHour], floored at [MIN_STREAK_RISK_HOUR].
     */
    private fun streakRiskHour(reminderHour: Int): Int =
        (reminderHour - 2).coerceAtLeast(MIN_STREAK_RISK_HOUR)

    private fun buildPendingIntent(requestCode: Int, action: String?): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            if (action != null) this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}