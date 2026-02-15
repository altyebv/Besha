package com.zeros.basheer.feature.practice.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.quizbank.domain.model.Question

/**
 * Card for Multiple Choice Questions.
 * User taps to select an option.
 */
@Composable
fun McqCard(
    question: Question,
    interactionState: QuestionInteractionState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val options = parseOptions(question.options ?: "")
    var selectedOption by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Question text
            Text(
                text = question.textAr,
                style = MaterialTheme.typography.titleLarge.copy(
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Options
            when (interactionState) {
                is QuestionInteractionState.Idle,
                is QuestionInteractionState.Interacting -> {
                    options.forEach { option ->
                        McqOption(
                            text = option,
                            isSelected = selectedOption == option,
                            state = McqOptionState.DEFAULT,
                            onClick = {
                                selectedOption = option
                                onAnswer(option)
                            }
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

                        McqOption(
                            text = option,
                            isSelected = false,
                            state = state,
                            onClick = { }
                        )
                    }

                    // Explanation
                    question.explanation?.let { exp ->
                        Spacer(modifier = Modifier.height(8.dp))
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Continue button
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("متابعة")
                    }
                }
            }
        }
    }
}

enum class McqOptionState {
    DEFAULT,
    CORRECT,
    INCORRECT,
    DISABLED
}

@Composable
private fun McqOption(
    text: String,
    isSelected: Boolean,
    state: McqOptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            McqOptionState.DEFAULT -> if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
            McqOptionState.CORRECT -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            McqOptionState.INCORRECT -> Color(0xFFF44336).copy(alpha = 0.2f)
            McqOptionState.DISABLED -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        },
        label = "optionBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when (state) {
            McqOptionState.DEFAULT -> if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
            McqOptionState.CORRECT -> Color(0xFF4CAF50)
            McqOptionState.INCORRECT -> Color(0xFFF44336)
            McqOptionState.DISABLED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        },
        label = "optionBorder"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        border = BorderStroke(2.dp, borderColor),
        enabled = state == McqOptionState.DEFAULT
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = when (state) {
                    McqOptionState.DISABLED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )

            // Result icon
            when (state) {
                McqOptionState.CORRECT -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                }
                McqOptionState.INCORRECT -> {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFFF44336)
                    )
                }
                else -> { }
            }
        }
    }
}

/**
 * Parse JSON options string to list
 */
private fun parseOptions(optionsJson: String): List<String> {
    return try {
        optionsJson
            .trim()
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
    } catch (e: Exception) {
        emptyList()
    }
}