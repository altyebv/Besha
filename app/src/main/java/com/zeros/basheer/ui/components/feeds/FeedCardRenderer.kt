package com.zeros.basheer.ui.components.feeds

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zeros.basheer.domain.model.CardInteractionState
import com.zeros.basheer.domain.model.FeedCard
import com.zeros.basheer.feature.feed.domain.model.FeedItemType
import com.zeros.basheer.feature.feed.domain.model.InteractionType

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
                    when (card.interactionType) {
                        InteractionType.SWIPE_TF -> {
                            TrueFalseCard(
                                card = card,
                                interactionState = interactionState,
                                onAnswer = onAnswer,
                                onContinue = onContinue
                            )
                        }
                        
                        InteractionType.MCQ -> {
                            McqCard(
                                card = card,
                                interactionState = interactionState,
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
