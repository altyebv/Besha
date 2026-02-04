package com.zeros.basheer.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Lessons : Screen("lessons")
    object Lab : Screen("lab")
    object Profile : Screen("profile")

    object LessonReader : Screen("lesson/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson/$lessonId"
    }

    object Feeds : Screen("feeds")
}