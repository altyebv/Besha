package com.zeros.basheer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zeros.basheer.feature.feed.presentation.FeedsScreen
import com.zeros.basheer.feature.quizbank.presentation.QuizBankScreen
import com.zeros.basheer.feature.quizbank.presentation.entry.ExamEntryScreen
import com.zeros.basheer.feature.quizbank.presentation.exam.ExamSessionScreen
import com.zeros.basheer.feature.quizbank.presentation.exam.ExamResultScreen
import com.zeros.basheer.feature.lesson.presentation.LessonsScreen
import com.zeros.basheer.feature.user.presentation.settings.SettingsScreen
import com.zeros.basheer.ui.screens.main.MainScreen
import com.zeros.basheer.ui.screens.main.MainViewModel
import com.zeros.basheer.feature.practice.presentation.PracticeSessionScreen
import com.zeros.basheer.feature.quizbank.presentation.builder.PracticeBuilderScreen
import com.zeros.basheer.ui.screens.profile.ProfileScreen
import com.zeros.basheer.feature.lesson.presentation.LessonReaderScreen
import com.zeros.basheer.feature.user.presentation.edit.EditProfileScreen
import com.zeros.basheer.feature.user.presentation.onboarding.OnboardingScreen

@Composable
fun BasheerNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    // ── Shared subjects list ──────────────────────────────────────────────────
    // MainViewModel is activity-scoped via Hilt, so this is the same instance
    // already used by MainScreen — no double loading.
    // `allSubjects` is used to populate the SubjectSwitcherStrip in Lessons
    // and QuizBank without requiring a dedicated subject-fetch in those screens.
    val mainViewModel: MainViewModel = hiltViewModel()
    val mainState by mainViewModel.state.collectAsStateWithLifecycle()
    val allSubjects = mainState.subjects.map { it.subject }

    // ── Last-visited subject — persists across bottom-nav taps ───────────────
    // When the user goes Home → Lessons → Home → Lessons, they land back on
    // the same subject they were looking at, not a blank state.
    var lastSubjectId by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ── Onboarding ────────────────────────────────────────────────────────
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Main ──────────────────────────────────────────────────────────────
        composable(Screen.Main.route) {
            MainScreen(
                onSubjectClick = { subjectId ->
                    lastSubjectId = subjectId
                    navController.navigate(Screen.Lessons.createRoute(subjectId))
                },
                navController = navController
            )
        }

        // ── Lessons ───────────────────────────────────────────────────────────
        // The old picker-gate (LessonsSubjectPicker) is gone. Navigating here
        // always shows the lessons list. If no subjectId is provided (bottom nav
        // tap), we fall back to lastSubjectId, or the first available subject.
        // The SubjectSwitcherStrip inside LessonsScreen handles inline switching.
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
            val subjectIdArg = backStackEntry.arguments?.getString("subjectId")
            val resolvedSubjectId = subjectIdArg ?: lastSubjectId

            // Remember the last viewed subject for future bottom-nav re-entries
            LaunchedEffect(resolvedSubjectId) {
                if (resolvedSubjectId != null) lastSubjectId = resolvedSubjectId
            }

            LessonsScreen(
                initialSubjectId = resolvedSubjectId,
                allSubjects = allSubjects,
                onLessonClick = { lessonId, partIndex ->
                    navController.navigate(Screen.LessonReader.createRoute(lessonId, partIndex))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Lesson Reader ─────────────────────────────────────────────────────
        composable(
            route = Screen.LessonReader.route,
            arguments = listOf(
                navArgument("lessonId") { type = NavType.StringType },
                navArgument("partIndex") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: return@composable
            val partIndex = backStackEntry.arguments?.getInt("partIndex") ?: 0
            LessonReaderScreen(
                lessonId = lessonId,
                initialPartIndex = partIndex,
                onBackClick = { navController.popBackStack() },
                onNavigateToNextPart = { nextPartIndex ->
                    navController.navigate(Screen.LessonReader.createRoute(lessonId, nextPartIndex)) {
                        popUpTo(Screen.LessonReader.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Feed ──────────────────────────────────────────────────────────────
        composable(Screen.Feeds.route) {
            FeedsScreen(onClose = { navController.popBackStack() })
        }

        // ── Quiz Bank ─────────────────────────────────────────────────────────
        // Same as Lessons — no picker gate. The SubjectSwitcherStrip in
        // QuizBankScreen handles inline switching. The optional subjectId arg
        // is still supported for deep links from recommendation cards.
        composable(
            route = Screen.QuizBank.route,
            arguments = listOf(
                navArgument("subjectId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val subjectIdArg = backStackEntry.arguments?.getString("subjectId")
                ?.takeIf { it.isNotBlank() && it != "{subjectId}" }

            QuizBankScreen(
                navController = navController,
                allSubjects = allSubjects,
                initialSubjectId = subjectIdArg ?: lastSubjectId
            )
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable(Screen.Profile.route) {
            ProfileScreen(
                onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // ── Edit Profile ──────────────────────────────────────────────────────
        composable(Screen.EditProfile.route) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(Screen.EditProfile.route) }
            )
        }

        // ── Practice ──────────────────────────────────────────────────────────
        composable(
            route = Screen.Practice.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            PracticeSessionScreen(
                onNavigateBack = { navController.popBackStack() },
                onSessionComplete = { },
                onRetryNavigate = { newSessionId ->
                    navController.navigate(Screen.Practice.createRoute(newSessionId)) {
                        popUpTo(Screen.Practice.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Practice Builder ──────────────────────────────────────────────────
        composable(
            route = Screen.PracticeBuilder.route,
            arguments = listOf(
                navArgument("subjectId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "CUSTOM"
                }
            )
        ) {
            PracticeBuilderScreen(
                onNavigateBack = { navController.popBackStack() },
                onSessionCreated = { sessionId ->
                    navController.navigate(Screen.Practice.createRoute(sessionId)) {
                        popUpTo(Screen.PracticeBuilder.baseRoute) { inclusive = true }
                    }
                }
            )
        }

        // ── Exam Entry ────────────────────────────────────────────────────────
        composable(
            route = Screen.ExamEntry.route,
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) {
            ExamEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartExam = { id ->
                    navController.navigate(Screen.ExamSession.createRoute(id)) {
                        popUpTo(Screen.ExamEntry.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Exam Session ──────────────────────────────────────────────────────
        composable(
            route = Screen.ExamSession.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType },
                navArgument("strictMode") { type = NavType.BoolType; defaultValue = false }
            )
        ) {
            ExamSessionScreen(
                onNavigateBack = { navController.popBackStack() },
                onExamComplete = { attemptId ->
                    navController.navigate(Screen.ExamResult.createRoute(attemptId)) {
                        popUpTo(Screen.QuizBank.route)
                    }
                }
            )
        }

        // ── Exam Result ───────────────────────────────────────────────────────
        composable(
            route = Screen.ExamResult.route,
            arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
        ) { backStackEntry ->
            val attemptId = backStackEntry.arguments?.getLong("attemptId") ?: return@composable
            ExamResultScreen(
                attemptId = attemptId,
                onExit = { navController.popBackStack(Screen.QuizBank.route, inclusive = false) },
                onRetry = { navController.popBackStack() }
            )
        }
    }
}