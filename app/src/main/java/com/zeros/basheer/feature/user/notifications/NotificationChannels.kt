package com.zeros.basheer.feature.user.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/**
 * Single source of truth for all Basheer notification channels.
 *
 * Call [createAll] once on app launch (idempotent — safe to call repeatedly).
 *
 * Channel strategy:
 *  - [DAILY_REMINDER]  → default importance, user-scheduled study nudge
 *  - [STREAK_ALERT]    → high importance, streak-at-risk warning (only fires if needed)
 *  - [ACHIEVEMENT]     → default importance, level-up / streak milestone celebrations
 */
object NotificationChannels {

    const val DAILY_REMINDER = "basheer_daily_reminder"
    const val STREAK_ALERT   = "basheer_streak_alert"
    const val ACHIEVEMENT    = "basheer_achievement"

    // Notification IDs — stable across app versions so Android can replace old ones
    const val ID_DAILY_REMINDER    = 1001
    const val ID_STREAK_RISK       = 1002
    const val ID_ACHIEVEMENT       = 1003
    const val ID_STREAK_MILESTONE  = 1004

    /**
     * Creates all channels. Idempotent — existing channels are left unchanged.
     * Call from [BasheerApp.onCreate] and/or [ReminderNotificationManager.createChannel].
     */
    fun createAll(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(
                DAILY_REMINDER,
                "تذكير يومي",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تذكير يومي بمراجعة دروس الشهادة"
                enableVibration(true)
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                STREAK_ALERT,
                "تنبيه السلسلة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تحذير عند اقتراب انقطاع سلسلة الدراسة"
                enableVibration(true)
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                ACHIEVEMENT,
                "الإنجازات",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تهانٍ عند الوصول إلى مستوى جديد أو تحقيق إنجاز"
                enableVibration(false)
            }
        )
    }
}