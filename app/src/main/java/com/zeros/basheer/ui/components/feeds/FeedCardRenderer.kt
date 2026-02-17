package com.zeros.basheer.ui.components.feeds

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zeros.basheer.feature.feed.domain.model.CardInteractionState
import com.zeros.basheer.feature.feed.domain.model.FeedCard
import com.zeros.basheer.feature.feed.domain.model.FeedItemType
import com.zeros.basheer.feature.feed.domain.model.InteractionType
import com.zeros.basheer.feature.practice.presentation.components.McqCard
import com.zeros.basheer.feature.practice.presentation.components.TrueFalseCard
import com.zeros.basheer.feature.practice.presentation.components.QuestionInteractionState
import com.zeros.basheer.feature.quizbank.domain.model.Question

/**
 * Central dispatcher for rendering feed cards based on their type.
 */
@Composable
fun FeedCardRenderer(
    card: FeedCard,
    interactionState: CardInteractionState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top bar with subject name and card type
        FeedCardTopBar(
            subjectName = card.subjectName,
            feedType = card.type
        )

        // Card content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (card.type) {
                FeedItemType.DEFINITION,
                FeedItemType.FACT,
                FeedItemType.DATE,
                FeedItemType.FORMULA,
                FeedItemType.RULE,
                FeedItemType.TIP -> {
                    DefinitionCard(
                        card = card,
                        onContinue = onContinue
                    )
                }

                FeedItemType.MINI_QUIZ -> {
                    // Convert FeedCard to Question for practice components
                    val question = card.toQuestion()
                    val questionInteractionState = interactionState.toQuestionInteractionState()

                    when (card.interactionType) {
                        InteractionType.SWIPE_TF -> {
                            TrueFalseCard(
                                question = question,
                                interactionState = questionInteractionState,
                                onAnswer = onAnswer,
                                onContinue = onContinue
                            )
                        }

                        InteractionType.MCQ -> {
                            McqCard(
                                question = question,
                                interactionState = questionInteractionState,
                                onAnswer = onAnswer,
                                onContinue = onContinue
                            )
                        }

                        InteractionType.TAP_CONFIRM,
                        InteractionType.MATCH,
                        null -> {
                            // Fallback to simple card
                            DefinitionCard(
                                card = card,
                                onContinue = onContinue
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Convert FeedCard to Question for practice components
 */
private fun FeedCard.toQuestion(): Question {
    return Question(
        id = this.id,
        subjectId = this.subjectId,
        unitId = null,
        lessonId = null,
        type = when (this.interactionType) {
            InteractionType.SWIPE_TF -> com.zeros.basheer.feature.quizbank.domain.model.QuestionType.TRUE_FALSE
            InteractionType.MCQ -> com.zeros.basheer.feature.quizbank.domain.model.QuestionType.MCQ
            else -> com.zeros.basheer.feature.quizbank.domain.model.QuestionType.MCQ
        },
        textAr = this.contentAr,
        textEn = this.contentEn,
        correctAnswer = this.correctAnswer ?: "",
        options = this.options?.let { opts ->
            // Convert List<String> to JSON string for Question model
            "[${opts.joinToString(",") { "\"$it\"" }}]"
        },
        explanation = this.explanation,
        imageUrl = this.imageUrl,
        tableData = null,
        source = com.zeros.basheer.feature.quizbank.domain.model.QuestionSource.ORIGINAL,
        sourceExamId = null,
        sourceDetails = null,
        sourceYear = null,
        difficulty = 2,
        cognitiveLevel = com.zeros.basheer.feature.quizbank.domain.model.CognitiveLevel.RECALL,
        points = 1,
        estimatedSeconds = 30,
        feedEligible = true
    )
}

/**
 * Convert CardInteractionState to QuestionInteractionState
 */
private fun CardInteractionState.toQuestionInteractionState(): QuestionInteractionState {
    return when (this) {
        is CardInteractionState.Idle -> QuestionInteractionState.Idle
        is CardInteractionState.Interacting -> QuestionInteractionState.Interacting
        is CardInteractionState.Answered -> QuestionInteractionState.Answered(
            userAnswer = this.userAnswer,
            isCorrect = this.isCorrect,
            explanation = this.explanation
        )
    }
}