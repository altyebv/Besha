package com.zeros.basheer.ui.navigation

sealed class Screen(val route: String) {
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

    object ExamSession : Screen("exam/{examId}") {
        fun createRoute(examId: String) = "exam/$examId"
    }

    object ExamResult : Screen("exam_result/{attemptId}") {
        fun createRoute(attemptId: Long) = "exam_result/$attemptId"
    }
}