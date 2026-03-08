package com.zeros.basheer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zeros.basheer.core.data.DatabaseSeeder
import com.zeros.basheer.core.ui.theme.BasheerTheme
import com.zeros.basheer.feature.analytics.AnalyticsManager
import com.zeros.basheer.feature.analytics.sync.AppLifecycleObserver
import com.zeros.basheer.feature.streak.domain.usecase.GetStreakStatusUseCase
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import com.zeros.basheer.feature.user.notifications.ReminderNotificationManager
import com.zeros.basheer.feature.user.notifications.ReminderScheduler
import com.zeros.basheer.ui.components.BasheerBottomBar
import com.zeros.basheer.ui.navigation.BasheerNavHost
import com.zeros.basheer.ui.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Routes where the bottom bar should be hidden (immersive/flow screens). */
private val bottomBarHiddenRoutes = setOf(
    Screen.Onboarding.route,
    Screen.EditProfile.route,
    Screen.Settings.route,
    Screen.LessonReader.route,
    Screen.ExamSession.route,
    Screen.ExamEntry.route,
    Screen.ExamResult.route,
    Screen.Feeds.route,
    "practice/{sessionId}"
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject lateinit var seeder: DatabaseSeeder
    @javax.inject.Inject lateinit var userPreferences: UserPreferencesRepository
    @javax.inject.Inject lateinit var notificationManager: ReminderNotificationManager
    @javax.inject.Inject lateinit var scheduler: ReminderScheduler
    @javax.inject.Inject lateinit var analyticsManager: AnalyticsManager
    @javax.inject.Inject lateinit var getStreakStatus: GetStreakStatusUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Seed database ─────────────────────────────────────────────────────
        // Guard: runs on first install (empty DB) OR when SEED_VERSION changes.
        // Bump SEED_VERSION in the string below whenever JSON content is updated
        // so structural changes (partIndex, new sections, etc.) propagate to
        // existing installs without a manual app-data clear.
        val SEED_VERSION = "1.1"
        lifecycleScope.launch {
            if (seeder.isDatabaseEmpty() || seeder.needsReseeding(this@MainActivity, SEED_VERSION)) {
                try {
                    seeder.seedFromAssets(this@MainActivity, "geographyy.json")
                    seeder.seedFromAssets(this@MainActivity, "military.json")
                    seeder.seedFromAssets(this@MainActivity, "physics.json")
                    seeder.seedFromAssets(this@MainActivity, "chemistry.json")
                    seeder.seedFromAssets(this@MainActivity, "arabic.json")
                    seeder.seedFromAssets(this@MainActivity, "islamic.json")
                    seeder.seedQuizBankFromAssets(this@MainActivity)
                    seeder.saveSeedVersion(this@MainActivity, SEED_VERSION)
                    Log.d("Seeder", "Database seeded successfully! (v$SEED_VERSION)")
                } catch (e: Exception) {
                    Log.e("Seeder", "Seeding failed", e)
                }
            }
        }

        // ── Notification channel (idempotent, safe on every launch) ──────────
        notificationManager.createChannel()

        // ── Reschedule alarm if notifications are on ──────────────────────────
        // Needed after app updates — the OS doesn't cancel alarms for updates,
        // but we reschedule anyway to pick up any time changes that happened
        // while the alarm wasn't active (e.g. after a clear-data).
        scheduler.rescheduleIfEnabled()

        // ── Notification re-engagement tracking ───────────────────────────────
        // If this launch came from a notification tap, record the engagement event.
        // Fires here (before setContent) so it runs exactly once on cold start.
        handleNotificationIntent(intent)

        enableEdgeToEdge()

        // ── Dark mode as reactive state ───────────────────────────────────────
        val darkModeFlow = userPreferences.isDarkMode()
            .stateIn(
                scope   = lifecycleScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

        val startDestination = if (userPreferences.hasCompletedOnboarding())
            Screen.Main.route else Screen.Onboarding.route

        setContent {
            val isDarkMode by darkModeFlow.collectAsState()

            BasheerTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute == null ||
                        bottomBarHiddenRoutes.none { pattern ->
                            routeMatchesPattern(currentRoute, pattern)
                        }

                Scaffold(
                    modifier  = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BasheerBottomBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    BasheerNavHost(
                        navController    = navController,
                        startDestination = startDestination,
                        modifier         = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    // ── Notification re-engagement ────────────────────────────────────────────

    /**
     * Handles warm launches from notification taps (singleTop back stack).
     * Cold launches are handled in [onCreate] directly.
     */
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    /**
     * Records [BasheerEvent.NotificationEngaged] if [intent] carries the
     * [ReminderScheduler.EXTRA_NOTIFICATION_TYPE] extra stamped by [ReminderScheduler].
     *
     * daysSinceLastOpen is computed from the epoch-ms written by
     * [AppLifecycleObserver] on each foreground — same SharedPreferences file.
     * This is the most accurate proxy available without a dedicated pref.
     */
    private fun handleNotificationIntent(intent: android.content.Intent?) {
        val notificationType = intent
            ?.getStringExtra(ReminderScheduler.EXTRA_NOTIFICATION_TYPE)
            ?: return  // Not a notification-driven launch — nothing to record.

        lifecycleScope.launch {
            val streakDays = runCatching { getStreakStatus().currentStreak }.getOrDefault(0)

            val lastOpenMillis = getSharedPreferences(
                AppLifecycleObserver.PREFS_NAME, android.content.Context.MODE_PRIVATE,
            ).getLong(AppLifecycleObserver.KEY_LAST_OPEN_MILLIS, 0L)

            val daysSinceLastOpen = if (lastOpenMillis == 0L) 0 else {
                val diffMs = System.currentTimeMillis() - lastOpenMillis
                (diffMs / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
            }

            analyticsManager.notificationEngaged(
                notificationType   = notificationType,
                daysSinceLastOpen  = daysSinceLastOpen,
                currentStreakDays  = streakDays,
            )
        }
    }
}

private fun routeMatchesPattern(currentRoute: String, pattern: String): Boolean {
    val liveBase     = currentRoute.substringBefore("?")
    val patternBase  = pattern.substringBefore("?")
    val liveSegs     = liveBase.split("/")
    val patternSegs  = patternBase.split("/")
    if (liveSegs.size != patternSegs.size) return false
    return liveSegs.zip(patternSegs).all { (live, pat) ->
        pat.startsWith("{") || live == pat
    }
}