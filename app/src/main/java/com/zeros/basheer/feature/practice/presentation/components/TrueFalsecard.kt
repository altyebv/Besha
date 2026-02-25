package com.zeros.basheer.feature.practice.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.quizbank.domain.model.Question

@Composable
fun TrueFalseCard(
    question: Question,
    interactionState: QuestionInteractionState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    feedMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 150f
    val isAnswered = interactionState is QuestionInteractionState.Answered

    val swipeBgColor by animateColorAsState(
        targetValue = when {
            dragOffset > swipeThreshold / 2  -> Color(0xFF22C55E).copy(alpha = 0.15f)
            dragOffset < -swipeThreshold / 2 -> Color(0xFFEF4444).copy(alpha = 0.15f)
            else                             -> Color.Transparent
        },
        animationSpec = tween(150),
        label = "bgColor"
    )

    val cardRotation by animateFloatAsState(
        targetValue = (dragOffset / 30f).coerceIn(-15f, 15f),
        animationSpec = tween(50),
        label = "rotation"
    )

    val questionTextColor = if (feedMode) Color.White.copy(alpha = 0.90f)
    else MaterialTheme.colorScheme.onSurface

    // Correct answer label for the panel ("صح" or "خطأ")
    val correctLabel = if (question.correctAnswer == "true") "صح ✓" else "خطأ ✗"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(swipeBgColor)
            .then(
                if (!isAnswered) Modifier.pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                dragOffset > swipeThreshold  -> onAnswer("true")
                                dragOffset < -swipeThreshold -> onAnswer("false")
                            }
                            dragOffset = 0f
                        },
                        onDragCancel = { dragOffset = 0f },
                        onHorizontalDrag = { _, amount -> dragOffset += amount }
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // ── Question text — dims when panel rises ──────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = if (feedMode && isAnswered) 200.dp else 0.dp)
                .alpha(if (feedMode && isAnswered) 0.35f else 1f)
                .then(
                    if (!isAnswered) Modifier.graphicsLayer {
                        rotationZ = cardRotation
                        translationX = dragOffset
                    } else Modifier
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                text = question.textAr,
                style = MaterialTheme.typography.headlineSmall.copy(
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                color = questionTextColor
            )
        }

        // ── Swipe hints (only while waiting for answer) ────────────────────
        if (!isAnswered) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SwipeHint("صح",  Icons.Default.Check, Color(0xFF22C55E), if (dragOffset < 30) 1f else 0.5f)
                SwipeHint("خطأ", Icons.Default.Close, Color(0xFFEF4444), if (dragOffset > -30) 1f else 0.5f)
            }
        }

        // ── Slide-up panel ─────────────────────────────────────────────────
        if (feedMode) {
            AnimatedVisibility(
                visible = isAnswered,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) { fullHeight -> fullHeight } + fadeIn(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                if (interactionState is QuestionInteractionState.Answered) {
                    FeedAnswerPanel(
                        isCorrect = interactionState.isCorrect,
                        correctAnswerLabel = if (!interactionState.isCorrect) correctLabel else null,
                        explanation = question.explanation,
                        onContinue = onContinue
                    )
                }
            }
        } else {
            // Practice mode — full result screen
            if (isAnswered && interactionState is QuestionInteractionState.Answered) {
                AnswerResult(
                    isCorrect = interactionState.isCorrect,
                    explanation = question.explanation,
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
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
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
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Icon(
            imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isCorrect) Color(0xFF22C55E) else Color(0xFFEF4444),
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = if (isCorrect) "إجابة صحيحة!" else "إجابة خاطئة",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isCorrect) Color(0xFF22C55E) else Color(0xFFEF4444)
        )
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
        Spacer(Modifier.height(16.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("متابعة") }
    }
}