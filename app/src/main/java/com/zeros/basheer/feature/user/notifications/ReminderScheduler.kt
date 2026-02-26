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
 * Schedules and cancels the daily study reminder alarm.
 *
 * Uses [AlarmManager.setRepeating] on older APIs and
 * [AlarmManager.setExactAndAllowWhileIdle] with manual re-schedule on API 23+
 * for battery-friendly but still precise delivery.
 *
 * The alarm fires [ReminderReceiver] at the user-configured hour:minute every day.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    private val context: Context,
    private val preferences: UserPreferencesRepository
) {

    companion object {
        private const val REQUEST_CODE = 2001
    }

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Schedules the daily reminder at the hour:minute stored in preferences.
     * If the time has already passed today, schedules for tomorrow.
     * Cancels any existing alarm first to avoid duplicates.
     */
    fun schedule(hour: Int, minute: Int) {
        cancel()  // Always cancel before rescheduling to avoid duplicates

        val triggerAt = nextAlarmTime(hour, minute)
        val intent = buildPendingIntent()

        when {
            // Android 12+ requires SCHEDULE_EXACT_ALARM to be granted by the user
            // in system settings. Check before using exact API — fall back to
            // setWindow (fires within a ~10 min window) if permission not granted.
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAt, intent
                    )
                } else {
                    // Inexact fallback — fires within a 10-minute window of the target time.
                    // Good enough for a daily study reminder.
                    alarmManager.setWindow(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        10 * 60 * 1000L, // 10 min window
                        intent
                    )
                }
            }
            // Android 6–11 — exact alarm allowed without special permission
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, intent
                )
            }
            // Android 5 and below — use repeating alarm
            else -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, triggerAt, AlarmManager.INTERVAL_DAY, intent
                )
            }
        }
    }

    /** Cancels the scheduled daily alarm, if any. */
    fun cancel() {
        alarmManager.cancel(buildPendingIntent())
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
                preferences.getReminderMinute().first()
            )
        }
        if (enabled) schedule(hour, minute) else cancel()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Returns the next timestamp (ms since epoch) for the given hour:minute.
     * If that time has already passed today, returns tomorrow's occurrence.
     */
    private fun nextAlarmTime(hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If we're already past this time today, push to tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis
    }

    private fun buildPendingIntent(): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}