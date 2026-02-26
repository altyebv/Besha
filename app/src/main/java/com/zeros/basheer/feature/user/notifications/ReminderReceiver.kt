package com.zeros.basheer.feature.user.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Receives the daily alarm broadcast and posts the reminder notification.
 *
 * Also handles [Intent.ACTION_BOOT_COMPLETED] — Android cancels all alarms
 * on device reboot, so we reschedule here automatically.
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

    @Inject
    lateinit var notificationManager: ReminderNotificationManager

    @Inject
    lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Device rebooted — reschedule the alarm if notifications are on.
                // We do this synchronously using the goAsync pattern is not needed
                // here since scheduler.rescheduleIfEnabled() is fast (prefs read).
                scheduler.rescheduleIfEnabled()
            }
            else -> {
                // Daily alarm fired — show the notification
                notificationManager.showReminderNotification()
            }
        }
    }
}