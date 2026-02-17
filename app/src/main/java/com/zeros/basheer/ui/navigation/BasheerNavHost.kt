package com.zeros.basheer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zeros.basheer.feature.feed.presentation.FeedsScreen
import com.zeros.basheer.feature.quizbank.presentation.QuizBankScreen
import com.zeros.basheer.feature.quizbank.presentation.exam.ExamSessionScreen
import com.zeros.basheer.feature.quizbank.presentation.exam.ExamResultScreen
import com.zeros.basheer.feature.lesson.presentation.LessonsScreen
import com.zeros.basheer.ui.components.common.LessonsSubjectPicker
import com.zeros.basheer.ui.screens.main.MainScreen
import com.zeros.basheer.feature.practice.presentation.PracticeSessionScreen
import com.zeros.basheer.ui.screens.profile.ProfileScreen
import com.zeros.basheer.feature.lesson.presentation.LessonReaderScreen

@Composable
fun BasheerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Remember last opened subject
    var lastSubjectId by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        modifier = modifier
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onSubjectClick = { subjectId ->
                    lastSubjectId = subjectId
                    navController.navigate(Screen.Lessons.createRoute(subjectId))
                },
                navController = navController
            )
        }

        composable(
            route = Screen.Lessons.route,
            arguments = listOf(
                navArgument("subjectId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: lastSubjectId

            if (subjectId == null) {
                // No subject selected, show subject picker
                LessonsSubjectPicker(
                    onSubjectSelected = { selectedId ->
                        lastSubjectId = selectedId
                        navController.navigate(Screen.Lessons.createRoute(selectedId)) {
                            popUpTo(Screen.Lessons.baseRoute) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                // Remember this subject for next time
                lastSubjectId = subjectId

                LessonsScreen(
                    subjectId = subjectId,
                    onLessonClick = { lessonId ->
                        navController.navigate(Screen.LessonReader.createRoute(lessonId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
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

        composable(
            route = Screen.ExamSession.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: return@composable
            ExamSessionScreen(
                onNavigateBack = { navController.popBackStack() },
                onExamComplete = { attemptId ->
                    navController.navigate(Screen.ExamResult.createRoute(attemptId)) {
                        popUpTo(Screen.QuizBank.route)
                    }
                }
            )
        }

        composable(
            route = Screen.ExamResult.route,
            arguments = listOf(
                navArgument("attemptId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val attemptId = backStackEntry.arguments?.getLong("attemptId") ?: return@composable
            ExamResultScreen(
                attemptId = attemptId,
                onExit = {
                    navController.popBackStack(Screen.QuizBank.route, inclusive = false)
                },
                onRetry = {
                    navController.popBackStack()
                }
            )
        }
    }
}