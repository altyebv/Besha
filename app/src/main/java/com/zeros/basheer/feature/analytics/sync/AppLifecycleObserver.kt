package com.zeros.basheer.feature.analytics.sync

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.zeros.basheer.feature.analytics.AnalyticsManager
import com.zeros.basheer.feature.streak.domain.usecase.GetStreakStatusUseCase
import com.zeros.basheer.feature.user.domain.usecase.GetUserXpUseCase
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
 */
@Singleton
class AppLifecycleObserver @Inject constructor(
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
            )

            lastOpenMillis = System.currentTimeMillis()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        analytics.onAppBackgrounded()
    }
}