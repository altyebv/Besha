package com.zeros.basheer.ui.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zeros.basheer.ui.screens.lab.LabScreen
import com.zeros.basheer.ui.screens.lessons.LessonsScreen
import com.zeros.basheer.ui.screens.main.MainScreen
import com.zeros.basheer.ui.screens.profile.ProfileScreen
import com.zeros.basheer.ui.screens.reader.LessonReaderScreen

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
                onLessonClick = { lessonId ->
                    navController.navigate(Screen.LessonReader.createRoute(lessonId))
                }
            )
        }

        composable(Screen.Lessons.route) {
            LessonsScreen(
                onLessonClick = { lessonId ->
                    navController.navigate(Screen.LessonReader.createRoute(lessonId))
                }
            )
        }

        composable(Screen.Lab.route) {
            LabScreen()
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
    }
}