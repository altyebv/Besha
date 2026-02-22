package com.zeros.basheer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zeros.basheer.core.data.DatabaseSeeder
import com.zeros.basheer.ui.components.BasheerBottomBar
import com.zeros.basheer.ui.navigation.BasheerNavHost
import com.zeros.basheer.ui.navigation.Screen
import com.zeros.basheer.core.ui.theme.BasheerTheme
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Routes where the bottom bar should be hidden (immersive/flow screens). */
private val bottomBarHiddenRoutes = setOf(
    Screen.Onboarding.route,
    Screen.EditProfile.route,
    Screen.LessonReader.route,
    Screen.ExamSession.route,
    Screen.ExamEntry.route,
    Screen.ExamResult.route,
    Screen.Feeds.route,
    "practice/{sessionId}"
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var seeder: DatabaseSeeder

    @Inject
    lateinit var userPreferences: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            try {
                seeder.seedFromAssets(this@MainActivity, "geographyy.json")
                seeder.seedFromAssets(this@MainActivity, "military.json")
                seeder.seedFromAssets(this@MainActivity, "physics.json")
                seeder.seedFromAssets(this@MainActivity, "chemistry.json")
                seeder.seedFromAssets(this@MainActivity, "arabic.json")
                seeder.seedFromAssets(this@MainActivity, "islamic.json")
                seeder.seedQuizBankFromAssets(this@MainActivity)
                Log.d("Lessons", "Database seeded successfully!")
            } catch (e: Exception) {
                Log.e("Seeds", "Seeding failed", e)
            }
        }
        enableEdgeToEdge()

        // Read synchronously — this is a single boolean pref read, safe on main thread
        val startDestination = if (userPreferences.hasCompletedOnboarding()) {
            Screen.Main.route
        } else {
            Screen.Onboarding.route
        }

        setContent {
            BasheerTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute == null ||
                        bottomBarHiddenRoutes.none { pattern ->
                            routeMatchesPattern(currentRoute, pattern)
                        }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BasheerBottomBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    BasheerNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

private fun routeMatchesPattern(currentRoute: String, pattern: String): Boolean {
    val liveBase = currentRoute.substringBefore("?")
    val patternBase = pattern.substringBefore("?")
    val liveSegments = liveBase.split("/")
    val patternSegments = patternBase.split("/")
    if (liveSegments.size != patternSegments.size) return false
    return liveSegments.zip(patternSegments).all { (live, pat) ->
        pat.startsWith("{") || live == pat
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainActivityPreview() {
    BasheerTheme {
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { BasheerBottomBar(navController = navController) }
        ) { innerPadding ->
            BasheerNavHost(
                navController = navController,
                startDestination = Screen.Main.route,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}