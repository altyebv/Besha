package com.zeros.basheer.feature.practice.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.quizbank.domain.model.Question
import kotlinx.serialization.json.Json

/**
 * Card for Fill in the Blank questions.
 * User taps to select an option that fills the blank.
 */
@Composable
fun FillBlankCard(
    question: Question,
    interactionState: QuestionInteractionState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val options = parseOptions(question.options ?: "")
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            when (interactionState) {
                is QuestionInteractionState.Idle,
                is QuestionInteractionState.Interacting -> {
                    // Question with blank
                    QuestionWithBlank(
                        text = question.textAr,
                        selectedOption = selectedOption
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instructions
                    Text(
                        text = "اختر الإجابة الصحيحة:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Options
                    options.forEach { option ->
                        FillBlankOption(
                            text = option,
                            isSelected = selectedOption == option,
                            state = FillBlankOptionState.DEFAULT,
                            onClick = {
                                selectedOption = option
                                onAnswer(option)
                            }
                        )
                    }
                }

                is QuestionInteractionState.Answered -> {
                    // Question with filled blank
                    QuestionWithBlank(
                        text = question.textAr,
                        selectedOption = interactionState.userAnswer,
                        isAnswered = true,
                        isCorrect = interactionState.isCorrect
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Show correct answer if wrong
                    if (!interactionState.isCorrect) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "الإجابة الصحيحة: ${question.correctAnswer}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
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

@Composable
private fun QuestionWithBlank(
    text: String,
    selectedOption: String?,
    isAnswered: Boolean = false,
    isCorrect: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Replace _____ with the selected option or keep it as blank
    val displayText = buildAnnotatedString {
        val blankPattern = "_____"
        val parts = text.split(blankPattern)

        if (parts.size > 1) {
            // Before blank
            append(parts[0])

            // The blank or filled answer
            if (selectedOption != null) {
                withStyle(
                    style = SpanStyle(
                        color = when {
                            !isAnswered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            isCorrect -> Color(0xFF4CAF50)
                            else -> Color(0xFFF44336)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                ) {
                    append(selectedOption)
                }
            } else {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("_____")
                }
            }

            // After blank
            if (parts.size > 1) {
                append(parts[1])
            }
        } else {
            // No blank found, show as is
            append(text)
        }
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.titleLarge.copy(
            lineHeight = 32.sp,
            fontWeight = FontWeight.Medium
        ),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )
}

enum class FillBlankOptionState {
    DEFAULT,
    CORRECT,
    INCORRECT,
    DISABLED
}

@Composable
private fun FillBlankOption(
    text: String,
    isSelected: Boolean,
    state: FillBlankOptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            FillBlankOptionState.DEFAULT -> if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
            FillBlankOptionState.CORRECT -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            FillBlankOptionState.INCORRECT -> Color(0xFFF44336).copy(alpha = 0.2f)
            FillBlankOptionState.DISABLED -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        },
        label = "optionBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when (state) {
            FillBlankOptionState.DEFAULT -> if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
            FillBlankOptionState.CORRECT -> Color(0xFF4CAF50)
            FillBlankOptionState.INCORRECT -> Color(0xFFF44336)
            FillBlankOptionState.DISABLED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        },
        label = "optionBorder"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        border = BorderStroke(2.dp, borderColor),
        enabled = state == FillBlankOptionState.DEFAULT
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
                    FillBlankOptionState.DISABLED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )

            // Result icon
            when (state) {
                FillBlankOptionState.CORRECT -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                }
                FillBlankOptionState.INCORRECT -> {
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
private fun parseOptions(optionsJson: String): List<String> = try {
    Json.decodeFromString<List<String>>(optionsJson.trim())
} catch (e: Exception) {
    emptyList()
}