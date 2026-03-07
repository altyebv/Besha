package com.zeros.basheer.feature.user.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zeros.basheer.feature.streak.domain.usecase.GetStreakStatusUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receives alarm broadcasts and dispatches the appropriate notification.
 *
 * Handles three cases:
 *  1. [Intent.ACTION_BOOT_COMPLETED] — device rebooted; reschedule both alarms.
 *  2. [ReminderScheduler.ACTION_STREAK_RISK] — streak-risk check time:
 *       - Reads [GetStreakStatusUseCase] asynchronously via [goAsync].
 *       - Only shows the high-importance streak-risk alert when [StreakStatus.isAtRisk]
 *         is true (user has an active streak but hasn't studied today yet).
 *  3. Default — daily reminder alarm fired; show the standard reminder with
 *       the current streak count embedded in the title.
 *
 * Must be declared in AndroidManifest.xml:
 * ```xml
 * <receiver
 *     android:name=".feature.user.notification.ReminderReceiver"
 *     android:exported="false">
 *     <intent-filter>
 *         <action android:name="android.intent.action.BOOT_COMPLETED"/>
 *     </intent-filter>
 * </receiver>
 * ```
 */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationManager: ReminderNotificationManager
    @Inject lateinit var scheduler: ReminderScheduler
    @Inject lateinit var getStreakStatus: GetStreakStatusUseCase

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {

            Intent.ACTION_BOOT_COMPLETED -> {
                // Device rebooted — Android cancels all alarms on reboot.
                // Reschedule if notifications are enabled (synchronous pref read, fast).
                scheduler.rescheduleIfEnabled()
            }

            ReminderScheduler.ACTION_STREAK_RISK -> {
                // Streak-risk check: only show the high-importance alert if the user
                // has an active streak but hasn't studied today. Uses goAsync() to
                // safely run a Room query from a BroadcastReceiver.
                val pending = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        val status = runCatching { getStreakStatus() }.getOrNull()
                        if (status?.isAtRisk == true && status.currentStreak > 0) {
                            notificationManager.showStreakRiskNotification(status.currentStreak)
                        }
                        // If isAtRisk is false the user already studied → no notification shown.
                    } finally {
                        pending.finish()
                    }
                }
            }

            else -> {
                // Daily reminder alarm fired.
                // Fetch streak asynchronously so the title can include the streak count,
                // then reschedule — both happen in the coroutine to keep the main thread free.
                val pending = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        val streakDays = runCatching {
                            getStreakStatus().currentStreak
                        }.getOrDefault(0)
                        notificationManager.showReminderNotification(streakDays)
                        // setExactAndAllowWhileIdle fires only once — re-arm tomorrow's alarm.
                        scheduler.rescheduleIfEnabled()
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }
}