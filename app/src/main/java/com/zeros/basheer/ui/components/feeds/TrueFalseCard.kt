package com.zeros.basheer.ui.components.feeds

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.domain.model.CardInteractionState
import com.zeros.basheer.domain.model.FeedCard
import kotlin.math.abs

/**
 * Card for True/False questions with swipe gestures.
 * Swipe right = True (صح)
 * Swipe left = False (خطأ)
 */
@Composable
fun TrueFalseCard(
    card: FeedCard,
    interactionState: CardInteractionState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 150f
    
    // Animate color based on drag direction
    val backgroundColor by animateColorAsState(
        targetValue = when {
            dragOffset > swipeThreshold / 2 -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            dragOffset < -swipeThreshold / 2 -> Color(0xFFF44336).copy(alpha = 0.2f)
            else -> Color.Transparent
        },
        animationSpec = tween(150),
        label = "bgColor"
    )
    
    val cardRotation by animateFloatAsState(
        targetValue = (dragOffset / 30f).coerceIn(-15f, 15f),
        animationSpec = tween(50),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .then(
                if (interactionState is CardInteractionState.Idle || 
                    interactionState is CardInteractionState.Interacting) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                when {
                                    dragOffset > swipeThreshold -> onAnswer("true")
                                    dragOffset < -swipeThreshold -> onAnswer("false")
                                }
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                dragOffset = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                dragOffset += dragAmount
                            }
                        )
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (interactionState) {
            is CardInteractionState.Idle,
            is CardInteractionState.Interacting -> {
                // Question content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .graphicsLayer {
                            rotationZ = cardRotation
                            translationX = dragOffset
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Text(
                        text = card.contentAr,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Swipe hints
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left hint (False)
                    SwipeHint(
                        text = "خطأ",
                        icon = Icons.Default.Close,
                        color = Color(0xFFF44336),
                        alpha = if (dragOffset < -30) 1f else 0.5f
                    )
                    
                    // Right hint (True)
                    SwipeHint(
                        text = "صح",
                        icon = Icons.Default.Check,
                        color = Color(0xFF4CAF50),
                        alpha = if (dragOffset > 30) 1f else 0.5f
                    )
                }
            }
            
            is CardInteractionState.Answered -> {
                // Result display
                AnswerResult(
                    isCorrect = interactionState.isCorrect,
                    explanation = interactionState.explanation,
                    onContinue = onContinue
                )
            }
        }
    }
}

@Composable
private fun SwipeHint(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    alpha: Float
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.graphicsLayer { this.alpha = alpha }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun AnswerResult(
    isCorrect: Boolean,
    explanation: String?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Result icon
        Icon(
            imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.size(72.dp)
        )
        
        // Result text
        Text(
            text = if (isCorrect) "إجابة صحيحة!" else "إجابة خاطئة",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
        
        // Explanation
        explanation?.let { exp ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = exp,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Continue button
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("متابعة")
        }
    }
}
