package com.zeros.basheer.feature.practice.presentation.components

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
 * Card for Compare questions.
 * User fills in comparison between two items.
 * Expected format in correctAnswer: JSON with compareData
 */
@Composable
fun CompareCard(
    question: Question,
    interactionState: QuestionInteractionState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Parse comparison structure from options/correctAnswer
    val compareData = remember(question) {
        parseCompareData(question.options ?: question.correctAnswer)
    }

    var userAnswers by remember {
        mutableStateOf(
            List(compareData.aspects.size) { "" to "" }
        )
    }
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

            // Comparison headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = compareData.item1Label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = compareData.item2Label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            when (interactionState) {
                is QuestionInteractionState.Idle,
                is QuestionInteractionState.Interacting -> {
                    // Input fields for each aspect
                    compareData.aspects.forEachIndexed { index, aspect ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = aspect,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = userAnswers[index].first,
                                    onValueChange = { newValue ->
                                        userAnswers = userAnswers.toMutableList().apply {
                                            this[index] = newValue to this[index].second
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("...") },
                                    singleLine = false,
                                    minLines = 2,
                                    maxLines = 4
                                )

                                OutlinedTextField(
                                    value = userAnswers[index].second,
                                    onValueChange = { newValue ->
                                        userAnswers = userAnswers.toMutableList().apply {
                                            this[index] = this[index].first to newValue
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("...") },
                                    singleLine = false,
                                    minLines = 2,
                                    maxLines = 4
                                )
                            }
                        }

                        if (index < compareData.aspects.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Submit button
                    val allFilled = userAnswers.all { it.first.isNotBlank() && it.second.isNotBlank() }
                    Button(
                        onClick = {
                            // Format answer as JSON
                            val answer = Json.encodeToString(
                                CompareAnswer.serializer(),
                                CompareAnswer(userAnswers.map {
                                    ComparisonPair(it.first, it.second)
                                })
                            )
                            onAnswer(answer)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = allFilled
                    ) {
                        Text("تحقق من الإجابة")
                    }
                }

                is QuestionInteractionState.Answered -> {
                    // Show correct answers
                    val correctAnswers = parseCompareAnswer(question.correctAnswer)

                    compareData.aspects.forEachIndexed { index, aspect ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = aspect,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Show correct answer or user's incorrect answer
                                AnswerDisplay(
                                    text = correctAnswers.getOrNull(index)?.first ?: "",
                                    modifier = Modifier.weight(1f)
                                )

                                AnswerDisplay(
                                    text = correctAnswers.getOrNull(index)?.second ?: "",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (index < compareData.aspects.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
private fun AnswerDisplay(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF4CAF50).copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            minLines = 2
        )
    }
}

// Data structures
@Serializable
data class CompareData(
    val item1Label: String,
    val item2Label: String,
    val aspects: List<String>
)

@Serializable
data class ComparisonPair(
    val first: String,
    val second: String
)

@Serializable
data class CompareAnswer(
    val comparisons: List<ComparisonPair>
)

// Parsing helpers
private fun parseCompareData(json: String): CompareData {
    return try {
        Json.decodeFromString<CompareData>(json)
    } catch (e: Exception) {
        // Fallback
        CompareData(
            item1Label = "العنصر الأول",
            item2Label = "العنصر الثاني",
            aspects = listOf("الجانب الأول", "الجانب الثاني")
        )
    }
}

private fun parseCompareAnswer(json: String): List<Pair<String, String>> {
    return try {
        val answer = Json.decodeFromString<CompareAnswer>(json)
        answer.comparisons.map { it.first to it.second }
    } catch (e: Exception) {
        emptyList()
    }
}