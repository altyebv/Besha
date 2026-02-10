package com.zeros.basheer.feature.quizbank.domain.model

data class QuizPerformanceSummary(
    val subjectId: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val averageScore: Float // 0f..1f
)
