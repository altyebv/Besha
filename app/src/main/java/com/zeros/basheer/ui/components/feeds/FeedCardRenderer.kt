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
import com.zeros.basheer.ui.screens.main.components.foundation.MainColors

/**
 * Central dispatcher for rendering feed cards based on their type.
 * Resolves subject color/emoji once here and passes them to all child cards.
 */
@Composable
fun FeedCardRenderer(
    card: FeedCard,
    interactionState: CardInteractionState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    onFlip: () -> Unit = {},
    onKnewIt: () -> Unit = {},
    onDidntKnow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Resolve once per card — consistent across top bar and card face
    val subjectColor = MainColors.subjectColorByName(card.subjectName, 0)
    val subjectEmoji = MainColors.subjectEmoji(card.subjectName)

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        FeedCardTopBar(
            subjectName = card.subjectName,
            feedType = card.type,
            subjectColor = subjectColor,
            subjectEmoji = subjectEmoji
        )

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
                        subjectColor = subjectColor,
                        onContinue = onContinue
                    )
                }

                FeedItemType.FLASH_CARD -> {
                    FlashCard(
                        card = card,
                        subjectColor = subjectColor,
                        interactionState = interactionState,
                        onFlip = onFlip,
                        onKnewIt = onKnewIt,
                        onDidntKnow = onDidntKnow
                    )
                }

                FeedItemType.MINI_QUIZ -> {
                    val question = card.toQuestion()
                    val questionInteractionState = interactionState.toQuestionInteractionState()

                    when (card.interactionType) {
                        InteractionType.SWIPE_TF -> {
                            TrueFalseCard(
                                question = question,
                                interactionState = questionInteractionState,
                                onAnswer = onAnswer,
                                onContinue = onContinue,
                                feedMode = true
                            )
                        }
                        InteractionType.MCQ -> {
                            McqCard(
                                question = question,
                                interactionState = questionInteractionState,
                                onAnswer = onAnswer,
                                onContinue = onContinue,
                                feedMode = true
                            )
                        }
                        InteractionType.TAP_CONFIRM,
                        InteractionType.MATCH,
                        null -> {
                            DefinitionCard(
                                card = card,
                                subjectColor = subjectColor,
                                onContinue = onContinue
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun FeedCard.toQuestion(): Question {
    return Question(
        id = this.id,
        subjectId = this.subjectId,
        unitId = null,
        lessonId = null,
        sectionId = null,
        type = when (this.interactionType) {
            InteractionType.SWIPE_TF -> com.zeros.basheer.feature.quizbank.domain.model.QuestionType.TRUE_FALSE
            InteractionType.MCQ -> com.zeros.basheer.feature.quizbank.domain.model.QuestionType.MCQ
            else -> com.zeros.basheer.feature.quizbank.domain.model.QuestionType.MCQ
        },
        textAr = this.contentAr,
        textEn = this.contentEn,
        correctAnswer = this.correctAnswer ?: "",
        options = this.options?.let { opts ->
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

private fun CardInteractionState.toQuestionInteractionState(): QuestionInteractionState {
    return when (this) {
        is CardInteractionState.Idle -> QuestionInteractionState.Idle
        is CardInteractionState.Interacting -> QuestionInteractionState.Interacting
        is CardInteractionState.Answered -> QuestionInteractionState.Answered(
            userAnswer = this.userAnswer,
            isCorrect = this.isCorrect,
            explanation = this.explanation
        )
        is CardInteractionState.Flipped -> QuestionInteractionState.Idle
    }
}