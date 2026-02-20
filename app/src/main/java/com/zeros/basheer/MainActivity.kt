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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Routes where the bottom bar should be hidden (immersive/flow screens). */
private val bottomBarHiddenRoutes = setOf(
    Screen.LessonReader.route,   // "lesson/{lessonId}"
    Screen.ExamSession.route,    // "exam/{examId}?strict={strictMode}"
    Screen.ExamEntry.route,      // "exam_entry/{examId}"
    Screen.ExamResult.route,     // "exam_result/{attemptId}"
    "practice/{sessionId}"
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var seeder: DatabaseSeeder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            try {
                seeder.seedFromAssets(this@MainActivity, "geographyy.json")
                seeder.seedFromAssets(this@MainActivity, "military.json")
                seeder.seedQuizBankFromAssets(this@MainActivity)
                Log.d("Lessons", "Database seeded successfully!")
            } catch (e: Exception) {
                Log.e("Seeds", "Seeding failed", e)
            }
        }
        enableEdgeToEdge()
        setContent {
            BasheerTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Hide the bottom bar on immersive screens (lesson reader, exams, practice)
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
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
/**
 * Checks whether a live route (e.g. "lesson/geo_unit1_lesson1") matches a
 * Navigation route pattern (e.g. "lesson/{lessonId}").
 * Strips query-string segments so optional params don't break the match.
 */
private fun routeMatchesPattern(currentRoute: String, pattern: String): Boolean {
    // Strip query params from both sides for comparison
    val liveBase = currentRoute.substringBefore("?")
    val patternBase = pattern.substringBefore("?")
    // Split into segments and check structural match
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
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}