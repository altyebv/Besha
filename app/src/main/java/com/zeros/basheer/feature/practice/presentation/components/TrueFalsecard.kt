package com.zeros.basheer.feature.practice.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.quizbank.domain.model.Question

private val CorrectGreen = Color(0xFF22C55E)
private val IncorrectRed = Color(0xFFEF4444)

// Feed-mode panel colors (dark, warm)
private val FeedPanelBg      = Color(0xFF18160F)
private val FeedPanelBgRight = Color(0xFF0F1A12)
private val FeedPanelBgWrong = Color(0xFF1A0F0F)

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
            dragOffset > swipeThreshold / 2  -> CorrectGreen.copy(alpha = 0.15f)
            dragOffset < -swipeThreshold / 2 -> IncorrectRed.copy(alpha = 0.15f)
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

    // The correct answer chip label for the panel
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
        // ── Question text ──────────────────────────────────────────────────────
        // Dims and pushes up when the feedback panel rises so it stays readable.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                // Reserve space at bottom so panel never fully covers the question
                .padding(bottom = if (isAnswered) 220.dp else 0.dp)
                .alpha(
                    when {
                        !isAnswered -> 1f
                        feedMode    -> 0.35f   // Feed: heavy dim, panel is the focus
                        else        -> 0.55f   // Exam: lighter dim, question still scannable
                    }
                )
                .then(
                    if (!isAnswered) Modifier.graphicsLayer {
                        rotationZ    = cardRotation
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

        // ── Swipe hints ────────────────────────────────────────────────────────
        if (!isAnswered) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SwipeHint("صح",  Icons.Default.Check, CorrectGreen, if (dragOffset < 30) 1f else 0.5f)
                SwipeHint("خطأ", Icons.Default.Close,  IncorrectRed,  if (dragOffset > -30) 1f else 0.5f)
            }
        }

        // ── Feedback panel — slides up from bottom in BOTH modes ───────────────
        AnimatedVisibility(
            visible = isAnswered,
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium
                )
            ) { fullHeight -> fullHeight } + fadeIn(animationSpec = tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (interactionState is QuestionInteractionState.Answered) {
                if (feedMode) {
                    // Dark warm panel — unchanged from original feed behavior
                    FeedAnswerPanel(
                        isCorrect          = interactionState.isCorrect,
                        correctAnswerLabel = if (!interactionState.isCorrect) correctLabel else null,
                        explanation        = question.explanation,
                        onContinue         = onContinue
                    )
                } else {
                    // Light surface panel — for practice / exam context
                    ExamAnswerPanel(
                        isCorrect          = interactionState.isCorrect,
                        correctAnswerLabel = if (!interactionState.isCorrect) correctLabel else null,
                        explanation        = question.explanation,
                        onContinue         = onContinue
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EXAM ANSWER PANEL
// Light-surface themed equivalent of FeedAnswerPanel.
// Matches Material3 surface colors so it sits naturally in the exam layout.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExamAnswerPanel(
    isCorrect: Boolean,
    correctAnswerLabel: String?,
    explanation: String?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isCorrect) CorrectGreen else IncorrectRed
    val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Colored top-border line drawn behind content
                .drawBehind {
                    drawRect(
                        color = accentColor,
                        size  = androidx.compose.ui.geometry.Size(size.width, 3.dp.toPx()),
                        topLeft = androidx.compose.ui.geometry.Offset.Zero
                    )
                }
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Verdict row ────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isCorrect) "إجابة صحيحة!" else "إجابة خاطئة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    if (!isCorrect && correctAnswerLabel != null) {
                        Text(
                            text = "الإجابة الصحيحة ↓",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }
                }
            }

            // ── Correct answer chip (only when wrong) ──────────────────────
            if (!isCorrect && correctAnswerLabel != null) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = CorrectGreen.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.30f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CorrectGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = correctAnswerLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CorrectGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Explanation ────────────────────────────────────────────────
            if (!explanation.isNullOrBlank()) {
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                    textAlign = TextAlign.Start
                )
            }

            // ── Continue button ────────────────────────────────────────────
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor   = if (isCorrect) Color(0xFF052E16) else Color(0xFF450A0A)
                ),
                shape = MaterialTheme.shapes.large,
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "متابعة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SWIPE HINT
// ─────────────────────────────────────────────────────────────────────────────

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