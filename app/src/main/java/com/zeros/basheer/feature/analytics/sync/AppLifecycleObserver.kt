package com.zeros.basheer.feature.analytics.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.zeros.basheer.feature.analytics.AnalyticsManager
import com.zeros.basheer.feature.streak.domain.usecase.GetStreakStatusUseCase
import com.zeros.basheer.feature.user.domain.usecase.GetUserXpUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Observes the app-level lifecycle (foreground/background) and fires the
 * corresponding analytics session events.
 *
 * Register this in [BasheerApp.onCreate] against [ProcessLifecycleOwner]:
 * ```kotlin
 * ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
 * ```
 *
 * On meaningful session end (>= [AnalyticsSyncWorker.MIN_SESSION_SECONDS_FOR_EARLY_SYNC]
 * seconds), an immediate one-time sync is scheduled so events reach Firestore
 * within minutes rather than waiting for the daily WorkManager window.
 */
@Singleton
class AppLifecycleObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analytics: AnalyticsManager,
    private val getStreakStatus: GetStreakStatusUseCase,
    private val getUserXp: GetUserXpUseCase,
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastOpenMillis: Long = 0L

    override fun onStart(owner: LifecycleOwner) {
        scope.launch {
            val streakStatus = runCatching { getStreakStatus() }.getOrNull()
            val xpSummary = runCatching { getUserXp().firstOrNull() }.getOrNull()

            val daysSinceLastOpen = if (lastOpenMillis == 0L) 0 else {
                val diffMs = System.currentTimeMillis() - lastOpenMillis
                (diffMs / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
            }

            analytics.onAppForegrounded(
                streakDays = streakStatus?.currentStreak ?: 0,
                totalXp = xpSummary?.totalXp ?: 0,
                level = xpSummary?.level ?: 1,
                studentPath = "UNKNOWN", // Refined in MainViewModel once profile loads
                daysSinceLastOpen = daysSinceLastOpen,
                appVersion = "",         // Filled by AnalyticsRepositoryImpl
                androidVersion = Build.VERSION.SDK_INT,
                deviceModel = Build.MODEL,
                networkType = resolveNetworkType(),
            )

            lastOpenMillis = System.currentTimeMillis()

            // Persist so MainActivity can compute daysSinceLastOpen for NotificationEngaged
            // without needing a separate repository or extra infrastructure.
            context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_LAST_OPEN_MILLIS, lastOpenMillis)
                .apply()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        val sessionDurationSeconds = analytics.onAppBackgrounded()

        // Trigger an early sync for meaningful sessions so data lands in Firestore
        // within minutes. Short/accidental opens are skipped to avoid burning writes.
        if (sessionDurationSeconds >= AnalyticsSyncWorker.MIN_SESSION_SECONDS_FOR_EARLY_SYNC) {
            AnalyticsSyncWorker.scheduleImmediate(context)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns "WIFI", "MOBILE", or "NONE" — safe on all API levels. */
    private fun resolveNetworkType(): String? = runCatching {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return@runCatching "NONE"
        val caps = cm.getNetworkCapabilities(network) ?: return@runCatching "NONE"
        return@runCatching when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> "WIFI"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "MOBILE"
            else -> "OTHER"
        }
    }.getOrNull()

    companion object {
        /** Shared with MainActivity for NotificationEngaged.daysSinceLastOpen. */
        const val PREFS_NAME           = "basheer_analytics"
        const val KEY_LAST_OPEN_MILLIS = "last_open_millis"
    }
}