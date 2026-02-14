package com.zeros.basheer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zeros.basheer.feature.feed.presentation.FeedsScreen
import com.zeros.basheer.feature.quizbank.presentation.QuizBankScreen
import com.zeros.basheer.feature.lesson.presentation.LessonsScreen
import com.zeros.basheer.ui.screens.main.MainScreen
import com.zeros.basheer.feature.practice.presentation.PracticeSessionScreen
import com.zeros.basheer.ui.screens.profile.ProfileScreen
import com.zeros.basheer.feature.lesson.presentation.LessonReaderScreen

@Composable
fun BasheerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        modifier = modifier
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onSubjectClick = { subjectId ->
                    // Navigate to lessons screen with subjectId
                    navController.navigate(Screen.Lessons.createRoute(subjectId))
                },
                navController = navController
            )
        }

        composable(
            route = Screen.Lessons.route,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: return@composable
            LessonsScreen(
                subjectId = subjectId,
                onLessonClick = { lessonId ->
                    navController.navigate(Screen.LessonReader.createRoute(lessonId))
                }
            )
        }

        composable(Screen.Feeds.route) {
            FeedsScreen(
                onClose = { navController.popBackStack() }
            )
        }

        composable(Screen.QuizBank.route) {
            QuizBankScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen()
        }

        composable(
            route = Screen.LessonReader.route,
            arguments = listOf(
                navArgument("lessonId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: return@composable
            LessonReaderScreen(
                lessonId = lessonId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "practice/{sessionId}",
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            PracticeSessionScreen(
                onNavigateBack = { navController.popBackStack() },
                onSessionComplete = { sessionId ->
                    // Already handled internally - results screen shows
                }
            )
        }
    }
}