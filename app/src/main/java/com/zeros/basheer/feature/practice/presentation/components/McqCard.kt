package com.zeros.basheer.feature.practice.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.Json

@Composable
fun McqCard(
    question: com.zeros.basheer.feature.quizbank.domain.model.Question,
    interactionState: QuestionInteractionState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    feedMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val options = parseOptions(question.options ?: "")
    var selectedOption by remember { mutableStateOf<String?>(null) }
    val isAnswered = interactionState is QuestionInteractionState.Answered

    val onText = if (feedMode) Color.White.copy(alpha = 0.90f) else MaterialTheme.colorScheme.onSurface

    if (feedMode) {
        Box(modifier = modifier.fillMaxSize()) {

            // ── Question + options — dim when panel is up ──────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
                    // Reserve space so options don't hide behind the rising panel
                    .padding(bottom = if (isAnswered) 200.dp else 0.dp)
                    .alpha(if (isAnswered) 0.45f else 1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = question.textAr,
                    style = MaterialTheme.typography.titleMedium.copy(
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center,
                    color = onText
                )
                Spacer(Modifier.height(4.dp))
                options.forEach { option ->
                    val state = when (interactionState) {
                        is QuestionInteractionState.Answered -> when {
                            option == question.correctAnswer  -> McqOptionState.CORRECT
                            option == interactionState.userAnswer && !interactionState.isCorrect -> McqOptionState.INCORRECT
                            else -> McqOptionState.DISABLED
                        }
                        else -> McqOptionState.DEFAULT
                    }
                    McqOption(
                        text = option,
                        isSelected = selectedOption == option,
                        state = state,
                        feedMode = true,
                        onClick = {
                            if (!isAnswered) {
                                selectedOption = option
                                onAnswer(option)
                            }
                        }
                    )
                }
            }

            // ── Slide-up answer panel ──────────────────────────────────────
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
                        correctAnswerLabel = question.correctAnswer,
                        explanation = question.explanation,
                        onContinue = onContinue
                    )
                }
            }
        }

    } else {
        // ── Practice layout (unchanged) ────────────────────────────────────
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = question.textAr,
                    style = MaterialTheme.typography.titleLarge.copy(
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                when (interactionState) {
                    is QuestionInteractionState.Idle,
                    is QuestionInteractionState.Interacting -> {
                        options.forEach { option ->
                            McqOption(
                                text = option,
                                isSelected = selectedOption == option,
                                state = McqOptionState.DEFAULT,
                                feedMode = false,
                                onClick = { selectedOption = option; onAnswer(option) }
                            )
                        }
                    }
                    is QuestionInteractionState.Answered -> {
                        options.forEach { option ->
                            val state = when {
                                option == question.correctAnswer -> McqOptionState.CORRECT
                                option == interactionState.userAnswer && !interactionState.isCorrect -> McqOptionState.INCORRECT
                                else -> McqOptionState.DISABLED
                            }
                            McqOption(text = option, isSelected = false, state = state, feedMode = false, onClick = {})
                        }
                        question.explanation?.let { exp ->
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = exp,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                            Text("متابعة")
                        }
                    }
                }
            }
        }
    }
}

enum class McqOptionState { DEFAULT, CORRECT, INCORRECT, DISABLED }

@Composable
private fun McqOption(
    text: String,
    isSelected: Boolean,
    state: McqOptionState,
    feedMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            McqOptionState.DEFAULT   -> if (isSelected) {
                if (feedMode) Color.White.copy(alpha = 0.10f) else MaterialTheme.colorScheme.primaryContainer
            } else {
                if (feedMode) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
            }
            McqOptionState.CORRECT   -> Color(0xFF22C55E).copy(alpha = 0.18f)
            McqOptionState.INCORRECT -> Color(0xFFEF4444).copy(alpha = 0.18f)
            McqOptionState.DISABLED  -> if (feedMode) Color.White.copy(alpha = 0.03f)
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        }, label = "optionBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when (state) {
            McqOptionState.DEFAULT   -> if (isSelected) {
                if (feedMode) Color.White.copy(alpha = 0.35f) else MaterialTheme.colorScheme.primary
            } else {
                if (feedMode) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
            McqOptionState.CORRECT   -> Color(0xFF22C55E)
            McqOptionState.INCORRECT -> Color(0xFFEF4444)
            McqOptionState.DISABLED  -> if (feedMode) Color.White.copy(alpha = 0.06f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        }, label = "optionBorder"
    )
    val textColor = when (state) {
        McqOptionState.DISABLED -> if (feedMode) Color.White.copy(alpha = 0.30f)
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        else                    -> if (feedMode) Color.White.copy(alpha = 0.88f)
        else MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        border = BorderStroke(1.5.dp, borderColor),
        enabled = state == McqOptionState.DEFAULT
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            when (state) {
                McqOptionState.CORRECT   -> Icon(Icons.Default.Check, null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                McqOptionState.INCORRECT -> Icon(Icons.Default.Close, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                else -> {}
            }
        }
    }
}

private fun parseOptions(optionsJson: String): List<String> = try {
    Json.decodeFromString<List<String>>(optionsJson.trim())
} catch (e: Exception) {
    emptyList()
}