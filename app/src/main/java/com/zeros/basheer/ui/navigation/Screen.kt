package com.zeros.basheer.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    object Main : Screen("main")
    object Lessons : Screen("lessons?subjectId={subjectId}") {
        const val baseRoute = "lessons"
        fun createRoute(subjectId: String) = "lessons?subjectId=$subjectId"
    }
    object Feeds : Screen("feeds")
    object QuizBank : Screen("quizbank")
    object Profile : Screen("profile")

    object LessonReader : Screen("lesson/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson/$lessonId"
    }

    object ExamEntry : Screen("exam_entry/{examId}") {
        fun createRoute(examId: String) = "exam_entry/$examId"
    }

    object ExamSession : Screen("exam/{examId}?strict={strictMode}") {
        fun createRoute(examId: String, strictMode: Boolean = false) = "exam/$examId?strict=$strictMode"
    }

    object ExamResult : Screen("exam_result/{attemptId}") {
        fun createRoute(attemptId: Long) = "exam_result/$attemptId"
    }
}