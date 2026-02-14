package com.zeros.basheer.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Lessons : Screen("lessons/{subjectId}") {
        fun createRoute(subjectId: String) = "lessons/$subjectId"
    }
    object Feeds : Screen("feeds")
    object QuizBank : Screen("quizbank")
    object Profile : Screen("profile")

    object LessonReader : Screen("lesson/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson/$lessonId"
    }
}