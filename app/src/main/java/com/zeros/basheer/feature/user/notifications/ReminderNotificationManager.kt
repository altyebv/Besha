package com.zeros.basheer.feature.user.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.zeros.basheer.MainActivity
import com.zeros.basheer.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Responsible for creating the notification channel and posting the daily
 * reminder notification. Called by [ReminderReceiver] when the alarm fires.
 *
 * Channel setup is idempotent — safe to call on every app launch.
 */
@Singleton
class ReminderNotificationManager @Inject constructor(
    private val context: Context
) {

    companion object {
        const val CHANNEL_ID   = "basheer_daily_reminder"
        const val NOTIFICATION_ID = 1001

        private const val CHANNEL_NAME        = "تذكير يومي"
        private const val CHANNEL_DESCRIPTION = "تذكير يومي بمراجعة دروس الشهادة"
    }

    /** Creates the notification channel. Safe to call multiple times. */
    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /** Posts the daily reminder notification. */
    fun showReminderNotification() {
        // Deep-link tap → opens MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(reminderTitle())
            .setContentText("استمر في سلسلتك — حلل درساً اليوم 📖")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("لا تقطع سلسلتك! خصص دقائق قليلة اليوم وراجع مادة من موادك 🔥")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // On Android 13+ POST_NOTIFICATIONS is a runtime permission — check before posting
        val canPost = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true  // Permission is implicit on API < 33
        }

        if (canPost) {
            @Suppress("MissingPermission")
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    /** Time-aware greeting for the notification title. */
    private fun reminderTitle(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11  -> "صباح المذاكرة! 🌅"
            in 12..16 -> "وقت المراجعة 📚"
            in 17..20 -> "مساء التحصيل 🌆"
            else      -> "هل راجعت اليوم؟ 🌙"
        }
    }
}