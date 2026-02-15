package com.zeros.basheer.feature.practice.presentation.components


/**
 * State of user interaction with a question.
 * Used by all question card components.
 */
sealed class QuestionInteractionState {
    /** Question just displayed, waiting for user */
    object Idle : QuestionInteractionState()

    /** User is interacting (e.g., selecting MCQ option) */
    object Interacting : QuestionInteractionState()

    /** User answered, showing result */
    data class Answered(
        val userAnswer: String,
        val isCorrect: Boolean,
        val explanation: String?
    ) : QuestionInteractionState()
}