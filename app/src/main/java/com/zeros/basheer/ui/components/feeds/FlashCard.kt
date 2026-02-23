package com.zeros.basheer.ui.components.feeds

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.feed.domain.model.CardInteractionState
import com.zeros.basheer.feature.feed.domain.model.FeedCard

@Composable
fun FlashCard(
    card: FeedCard,
    interactionState: CardInteractionState,
    onFlip: () -> Unit,
    onKnewIt: () -> Unit,
    onDidntKnow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFlipped = interactionState is CardInteractionState.Flipped ||
            interactionState is CardInteractionState.Answered

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Card — tapping flips it via fade crossfade
        AnimatedContent(
            targetState = isFlipped,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "flash_card_content"
        ) { flipped ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clickable(
                        enabled = !flipped,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onFlip() },
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = if (flipped)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (flipped) (card.back ?: "") else card.contentAr,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (flipped) FontWeight.Normal else FontWeight.Bold,
                            fontSize = if (flipped) 20.sp else 26.sp,
                            lineHeight = 34.sp
                        ),
                        color = if (flipped)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (!isFlipped) {
            Text(
                text = "اضغط للكشف",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "هل كنت تعرف الإجابة؟",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDidntKnow,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935).copy(alpha = 0.15f),
                            contentColor = Color(0xFFE53935)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("لم أعرف", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onKnewIt,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF43A047).copy(alpha = 0.15f),
                            contentColor = Color(0xFF43A047)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("عرفت", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}