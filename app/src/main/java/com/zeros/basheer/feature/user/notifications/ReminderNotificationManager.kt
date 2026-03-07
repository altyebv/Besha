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
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Posts the daily study reminder and the streak-at-risk alert.
 *
 * ── Daily reminder ────────────────────────────────────────────────────────────
 * Fires at the user's configured time. Content rotates through a pool of
 * messages keyed by day-of-week so the notification never reads the same
 * twice in a row. When the user has an active streak, the title includes
 * the streak count to reinforce the habit.
 *
 * ── Streak-risk alert ────────────────────────────────────────────────────────
 * Fires ~2 hours before the user's configured reminder IF they haven't
 * studied yet today. Uses [NotificationChannels.STREAK_ALERT] (high importance)
 * to make sure it's noticed. Only shown when [streakDays] > 0 AND the
 * receiver's [ReminderReceiver] has confirmed no activity today.
 *
 * Channel setup is idempotent — [NotificationChannels.createAll] is safe to
 * call on every app launch.
 */
@Singleton
class ReminderNotificationManager @Inject constructor(
    private val context: Context
) {

    // ── Channel setup (kept for backward-compat, delegates to central registry) ──

    fun createChannel() = NotificationChannels.createAll(context)

    // ── Daily reminder ────────────────────────────────────────────────────────

    /**
     * Posts the daily study reminder.
     *
     * @param streakDays Current streak count. 0 = no active streak.
     *                   Passed in by [ReminderReceiver] after an async streak check.
     */
    fun showReminderNotification(streakDays: Int = 0) {
        val title = reminderTitle(streakDays)
        val body  = reminderBody()

        post(
            notificationId = NotificationChannels.ID_DAILY_REMINDER,
            channelId      = NotificationChannels.DAILY_REMINDER,
            title          = title,
            body           = body,
            priority       = NotificationCompat.PRIORITY_DEFAULT,
        )
    }

    // ── Streak-at-risk ────────────────────────────────────────────────────────

    /**
     * Posts a high-importance streak-at-risk warning.
     * Only call this when confirmed the user hasn't studied today.
     *
     * @param streakDays Current streak count (must be > 0 to call this).
     */
    fun showStreakRiskNotification(streakDays: Int) {
        val title = "سلسلتك في خطر! 🔥"
        val body  = streakRiskBody(streakDays)

        post(
            notificationId = NotificationChannels.ID_STREAK_RISK,
            channelId      = NotificationChannels.STREAK_ALERT,
            title          = title,
            body           = body,
            priority       = NotificationCompat.PRIORITY_HIGH,
        )
    }

    // ── Core post ─────────────────────────────────────────────────────────────

    private fun post(
        notificationId: Int,
        channelId: String,
        title: String,
        body: String,
        priority: Int,
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
            .setPriority(priority)
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

    // ── Content generation ────────────────────────────────────────────────────

    /**
     * Time-aware + streak-aware title.
     * If the user has a streak, the title celebrates it; otherwise it's a general nudge.
     */
    private fun reminderTitle(streakDays: Int): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeGreeting = when (hour) {
            in 5..11  -> "صباح المذاكرة! 🌅"
            in 12..16 -> "وقت المراجعة 📚"
            in 17..20 -> "مساء التحصيل 🌆"
            else      -> "هل راجعت اليوم؟ 🌙"
        }
        return if (streakDays >= 2) "$timeGreeting — اليوم $streakDays 🔥" else timeGreeting
    }

    /**
     * Rotates through a pool of motivational bodies by day-of-week so the
     * notification never reads the same two days in a row.
     */
    private fun reminderBody(): String {
        val dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) % REMINDER_POOL.size
        return REMINDER_POOL[dayIndex]
    }

    private fun streakRiskBody(streakDays: Int): String {
        val plural = if (streakDays == 1) "يوم واحد" else "$streakDays أيام"
        return "سلسلتك من $plural ستنقطع الليلة إن لم تذاكر. دقيقة واحدة تكفي — افتح بشير الآن."
    }

    // ── Message pool ──────────────────────────────────────────────────────────

    companion object {
        private val REMINDER_POOL = listOf(
            "لا تقطع سلسلتك! خصص دقائق قليلة اليوم وراجع مادة من موادك 🔥",
            "كل مراجعة قصيرة اليوم تُوفّر عليك ساعات من الحفظ غداً — افتح بشير الآن 📖",
            "أكثر الطلاب نجاحاً لا يذاكرون أكثر، بل يذاكرون بانتظام. لا تفوّت يومك 🎯",
            "دروس اليوم قصيرة وممتعة — راجع درساً واحداً واكسب نقاط جديدة ⭐",
            "الشهادة تقترب — كل مراجعة اليوم تُقرّبك خطوة من النجاح 🏆",
            "عقلك يتعلم بشكل أعمق حين تراجع يومياً. لا تُضيّع هذه الفرصة 💡",
            "لحظة واحدة من الجهد الآن تعادل ساعات من الكدّ في آخر اللحظات — ذاكر الآن 📝",
        )
    }
}