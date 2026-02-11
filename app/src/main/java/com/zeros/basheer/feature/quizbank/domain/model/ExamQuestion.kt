package com.zeros.basheer.feature.quizbank.domain.model

data class ExamQuestion(
    val examId: String,
    val questionId: String,
    val order: Int,
    val sectionLabel: String? = null,
    val points: Int? = null
)