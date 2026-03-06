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
    object Profile : Screen("profile")

    /**
     * partIndex defaults to 0. LessonsScreen passes the next incomplete part index
     * so the user always lands on the right part when tapping a lesson.
     */
    object LessonReader : Screen("lesson/{lessonId}?part={partIndex}") {
        fun createRoute(lessonId: String, partIndex: Int = 0) =
            "lesson/$lessonId?part=$partIndex"
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

    /**
     * Practice session screen. Always requires a [sessionId] (Long).
     * Sessions are created before navigation — callers create a session
     * via a use case, then navigate here with the returned ID.
     */
    object Practice : Screen("practice/{sessionId}") {
        fun createRoute(sessionId: Long) = "practice/$sessionId"
    }

    /**
     * Quiz bank screen. [subjectId] is optional.
     * - Bottom nav: navigate to [route] directly — no subject context.
     * - From a recommendation: use [createRoute] with a subjectId — scopes the screen
     *   to that subject so QuizBankViewModel loads the right data.
     *
     * Registered as a single composable with a nullable subjectId argument so
     * Compose Navigation treats it as one route, not two.
     */
    object QuizBank : Screen("quizbank?subjectId={subjectId}") {
        /** Navigate without subject context (e.g. bottom nav). */
        const val baseRoute = "quizbank"
        /** Navigate scoped to a specific subject (e.g. from a recommendation). */
        fun createRoute(subjectId: String) = "quizbank?subjectId=$subjectId"
    }
}