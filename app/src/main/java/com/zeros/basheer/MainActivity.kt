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
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import com.zeros.basheer.feature.user.notification.ReminderNotificationManager
import com.zeros.basheer.feature.user.notification.ReminderScheduler
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Seed database (first launch only) ────────────────────────────────
        lifecycleScope.launch {
            if (seeder.isDatabaseEmpty()) {
                try {
                    seeder.seedFromAssets(this@MainActivity, "geographyy.json")
                    seeder.seedFromAssets(this@MainActivity, "military.json")
                    seeder.seedFromAssets(this@MainActivity, "physics.json")
                    seeder.seedFromAssets(this@MainActivity, "chemistry.json")
                    seeder.seedFromAssets(this@MainActivity, "arabic.json")
                    seeder.seedFromAssets(this@MainActivity, "islamic.json")
                    seeder.seedQuizBankFromAssets(this@MainActivity)
                    Log.d("Seeder", "Database seeded successfully!")
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