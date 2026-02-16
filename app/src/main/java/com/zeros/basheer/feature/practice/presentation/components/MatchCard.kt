package com.zeros.basheer.feature.practice.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Card for Match questions.
 * User taps items to match pairs.
 * options format: JSON with leftItems and rightItems
 */
@Composable
fun MatchCard(
    question: Question,
    interactionState: QuestionInteractionState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val matchData = remember(question) {
        parseMatchData(question.options ?: "")
    }

    // Track selected items and matches
    var selectedLeft by remember { mutableIntStateOf(-1) }
    var selectedRight by remember { mutableIntStateOf(-1) }
    var matches by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }

    val scrollState = rememberScrollState()

    // Auto-match when both sides selected
    LaunchedEffect(selectedLeft, selectedRight) {
        if (selectedLeft >= 0 && selectedRight >= 0) {
            // Add match
            matches = matches + (selectedLeft to selectedRight)
            selectedLeft = -1
            selectedRight = -1
        }
    }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            Text(
                text = "اضغط على عنصر من كل جانب لمطابقتهما",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            when (interactionState) {
                is QuestionInteractionState.Idle,
                is QuestionInteractionState.Interacting -> {
                    // Match interface
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Left column
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            matchData.leftItems.forEachIndexed { index, item ->
                                val isMatched = matches.any { it.first == index }
                                val isSelected = selectedLeft == index

                                MatchItem(
                                    text = item,
                                    isSelected = isSelected,
                                    isMatched = isMatched,
                                    onClick = {
                                        if (!isMatched) {
                                            selectedLeft = if (isSelected) -1 else index
                                        }
                                    }
                                )
                            }
                        }

                        // Right column
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            matchData.rightItems.forEachIndexed { index, item ->
                                val isMatched = matches.any { it.second == index }
                                val isSelected = selectedRight == index

                                MatchItem(
                                    text = item,
                                    isSelected = isSelected,
                                    isMatched = isMatched,
                                    onClick = {
                                        if (!isMatched) {
                                            selectedRight = if (isSelected) -1 else index
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Reset button
                    if (matches.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                matches = emptyList()
                                selectedLeft = -1
                                selectedRight = -1
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("إعادة تعيين")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Submit button
                    val allMatched = matches.size == matchData.leftItems.size
                    Button(
                        onClick = {
                            // Format answer as JSON
                            val answer = Json.encodeToString(
                                MatchAnswer.serializer(),
                                MatchAnswer(matches)
                            )
                            onAnswer(answer)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = allMatched
                    ) {
                        Text("تحقق من الإجابة")
                    }
                }

                is QuestionInteractionState.Answered -> {
                    // Show correct matches
                    val correctMatches = parseMatchAnswer(question.correctAnswer)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Left column with correct matches
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            correctMatches.forEach { (leftIndex, _) ->
                                MatchItemDisplay(
                                    text = matchData.leftItems.getOrNull(leftIndex) ?: "",
                                    isCorrect = true
                                )
                            }
                        }

                        // Connection indicator
                        Column(
                            modifier = Modifier.width(40.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            correctMatches.forEach { _ ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "⟷",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }

                        // Right column with correct matches
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            correctMatches.forEach { (_, rightIndex) ->
                                MatchItemDisplay(
                                    text = matchData.rightItems.getOrNull(rightIndex) ?: "",
                                    isCorrect = true
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
private fun MatchItem(
    text: String,
    isSelected: Boolean,
    isMatched: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isMatched -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isMatched -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        label = "borderColor"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        border = BorderStroke(2.dp, borderColor),
        enabled = !isMatched
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = if (isMatched) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun MatchItemDisplay(
    text: String,
    isCorrect: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) {
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            } else {
                Color(0xFFF44336).copy(alpha = 0.1f)
            }
        ),
        border = BorderStroke(
            1.dp,
            if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Data structures
@Serializable
data class MatchData(
    val leftItems: List<String>,
    val rightItems: List<String>
)

@Serializable
data class MatchAnswer(
    val matches: List<Pair<Int, Int>>
)

// Parsing helpers
private fun parseMatchData(json: String): MatchData {
    return try {
        Json.decodeFromString<MatchData>(json)
    } catch (e: Exception) {
        // Fallback
        MatchData(
            leftItems = listOf("العنصر 1", "العنصر 2", "العنصر 3"),
            rightItems = listOf("المطابق 1", "المطابق 2", "المطابق 3")
        )
    }
}

private fun parseMatchAnswer(json: String): List<Pair<Int, Int>> {
    return try {
        val answer = Json.decodeFromString<MatchAnswer>(json)
        answer.matches
    } catch (e: Exception) {
        emptyList()
    }
}