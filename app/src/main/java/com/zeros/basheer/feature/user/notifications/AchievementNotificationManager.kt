package com.zeros.basheer.feature.user.notification

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
 * Posts achievement notifications for XP level-ups and streak milestones.
 *
 * Called from [AnalyticsManager] immediately after the corresponding analytics
 * event is enqueued, so the notification fires in the same moment the achievement
 * is recorded — no extra wiring needed in individual ViewModels.
 *
 * Channel: [NotificationChannels.ACHIEVEMENT] (IMPORTANCE_DEFAULT — celebratory,
 * not urgent, so we don't buzz loudly).
 */
@Singleton
class AchievementNotificationManager @Inject constructor(
    private val context: Context
) {

    /**
     * Celebrates an XP level-up.
     *
     * @param newLevel The level the user just reached (1-indexed).
     * @param totalXp  Their cumulative XP — shown as social proof.
     */
    fun showLevelUp(newLevel: Int, totalXp: Int) {
        val title = "ارتقيت إلى المستوى $newLevel! 🏆"
        val body  = "مجموع نقاطك الآن $totalXp XP — واصل التحدي وابلغ المستوى ${newLevel + 1}."

        post(
            notificationId = NotificationChannels.ID_ACHIEVEMENT,
            channelId      = NotificationChannels.ACHIEVEMENT,
            title          = title,
            body           = body,
        )
    }

    /**
     * Celebrates a streak milestone (3, 7, 14, 30, 60, 100 days).
     *
     * @param streakDays  The milestone value that was just crossed.
     * @param streakLevel "SPARK" or "FLAME" — affects the congratulations copy.
     */
    fun showStreakMilestone(streakDays: Int, streakLevel: String) {
        val emoji = if (streakLevel == "FLAME") "🔥🔥" else "🔥"
        val title = "سلسلة $streakDays يوم متواصل! $emoji"
        val body  = milestoneBody(streakDays)

        post(
            notificationId = NotificationChannels.ID_STREAK_MILESTONE,
            channelId      = NotificationChannels.ACHIEVEMENT,
            title          = title,
            body           = body,
        )
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private fun post(
        notificationId: Int,
        channelId: String,
        title: String,
        body: String,
    ) {
        if (!canPost()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        @Suppress("MissingPermission")
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun canPost(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else true

    private fun milestoneBody(streakDays: Int): String = when (streakDays) {
        3    -> "ثلاثة أيام على التوالي — بداية ممتازة! استمر وستصبح المراجعة عادة راسخة."
        7    -> "أسبوع كامل من المذاكرة! تثبت كل يوم أن الاستمرار هو المفتاح."
        14   -> "أسبوعان متواصلان — عقلك يبني روابط معرفية عميقة الآن."
        30   -> "شهر من الانضباط! طالب الشهادة الحقيقي لا يتوقف."
        60   -> "شهران بلا انقطاع — وصلت إلى مستوى من الجدية يحسدك عليه كثيرون."
        100  -> "مئة يوم! 💯 هذا إنجاز استثنائي — أنت على الطريق الصحيح تماماً."
        else -> "رقم مميز! واصل مسيرتك ولا تتوقف."
    }
}