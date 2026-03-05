package com.zeros.basheer.feature.lesson.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.lesson.presentation.CheckpointUiState
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import org.json.JSONArray

/**
 * Inline checkpoint gate rendered after a section's last block in the lesson reader.
 *
 * - MCQ: tappable option list → "تحقق" button → correct/wrong feedback + hint.
 * - ORDER: drag-to-rearrange list → same flow.
 *
 * Soft gate: "متابعة" always unlocks regardless of correctness.
 * Wrong answer shows the [explanation] field as a remediation hint pointing back to the section.
 */
@Composable
fun CheckpointCard(
    sectionTitle: String,
    state: CheckpointUiState,
    onSelect: (String) -> Unit,
    onSubmit: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val correctColor = Color(0xFF10B981)   // emerald — matches app Success color
    val wrongColor = Color(0xFFEF4444)     // error red

    val feedbackColor = when (state.isCorrect) {
        true -> correctColor
        false -> wrongColor
        null -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (state.submitted) feedbackColor.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────────
            CheckpointHeader(submitted = state.submitted, isCorrect = state.isCorrect)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            // ── Question text ────────────────────────────────────────────────
            Text(
                text = state.question.textAr,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    lineHeight = 28.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start
            )

            // ── Question interaction ─────────────────────────────────────────
            when (state.question.type) {
                QuestionType.MCQ -> McqOptions(
                    question = state.question,
                    selected = state.selected,
                    submitted = state.submitted,
                    isCorrect = state.isCorrect,
                    onSelect = onSelect
                )
                QuestionType.ORDER -> OrderItems(
                    question = state.question,
                    submitted = state.submitted,
                    isCorrect = state.isCorrect,
                    onOrderChanged = onSelect
                )
                else -> Unit
            }

            // ── Feedback strip ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = state.submitted,
                enter = fadeIn() + expandVertically()
            ) {
                FeedbackStrip(
                    isCorrect = state.isCorrect == true,
                    explanation = state.question.explanation,
                    sectionTitle = sectionTitle
                )
            }

            // ── Action button ────────────────────────────────────────────────
            if (!state.submitted) {
                Button(
                    onClick = onSubmit,
                    enabled = state.selected != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تحقق", fontWeight = FontWeight.SemiBold)
                }
            } else {
                OutlinedButton(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, feedbackColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (state.isCorrect == true) "رائع، متابعة ↓" else "حسناً، متابعة ↓",
                        color = feedbackColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── Header chip ──────────────────────────────────────────────────────────────

@Composable
private fun CheckpointHeader(submitted: Boolean, isCorrect: Boolean?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val icon = when {
            !submitted -> Icons.Outlined.Quiz
            isCorrect == true -> Icons.Filled.Check
            else -> Icons.Filled.Close
        }
        val tint = when {
            !submitted -> MaterialTheme.colorScheme.primary
            isCorrect == true -> Color(0xFF10B981)
            else -> Color(0xFFEF4444)
        }
        val label = when {
            !submitted -> "تحقق من فهمك"
            isCorrect == true -> "إجابة صحيحة!"
            else -> "راجع القسم"
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = tint
        )
    }
}

// ── MCQ options ──────────────────────────────────────────────────────────────

@Composable
private fun McqOptions(
    question: Question,
    selected: String?,
    submitted: Boolean,
    isCorrect: Boolean?,
    onSelect: (String) -> Unit
) {
    val options = remember(question.options) { parseJsonOptions(question.options ?: "") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            val optionColor = when {
                !submitted -> if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
                option == question.correctAnswer -> Color(0xFFD1FAE5)   // correct
                isSelected && isCorrect == false -> Color(0xFFFFE4E6)   // wrong selection
                else -> MaterialTheme.colorScheme.surface
            }
            val borderColor = when {
                !submitted -> if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                option == question.correctAnswer -> Color(0xFF10B981)
                isSelected && isCorrect == false -> Color(0xFFEF4444)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            }

            Surface(
                onClick = { if (!submitted) onSelect(option) },
                enabled = !submitted,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = optionColor,
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Result icon after submission
                    if (submitted) {
                        when {
                            option == question.correctAnswer ->
                                Icon(Icons.Filled.Check, null,
                                    tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            isSelected ->
                                Icon(Icons.Filled.Close, null,
                                    tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            else -> Spacer(Modifier.size(16.dp))
                        }
                    }
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ── ORDER items ──────────────────────────────────────────────────────────────

@Composable
private fun OrderItems(
    question: Question,
    submitted: Boolean,
    isCorrect: Boolean?,
    onOrderChanged: (String) -> Unit
) {
    val originalItems = remember(question.options) {
        parseJsonOptions(question.options ?: "").shuffled()
    }
    var items by remember { mutableStateOf(originalItems) }

    // Emit serialized order on every change
    LaunchedEffect(items) {
        if (!submitted) onOrderChanged(items.joinToString(","))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEachIndexed { index, item ->
            val itemColor = when {
                !submitted -> MaterialTheme.colorScheme.surface
                isCorrect == true -> Color(0xFFD1FAE5)
                else -> Color(0xFFFFE4E6)
            }
            val borderColor = when {
                !submitted -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                isCorrect == true -> Color(0xFF10B981)
                else -> Color(0xFFEF4444)
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = itemColor,
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Step number pill
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!submitted) {
                        // Simple up/down swap buttons — avoids complex drag state inside LazyColumn
                        Column {
                            IconButton(
                                onClick = {
                                    if (index > 0) {
                                        items = items.toMutableList().also {
                                            val tmp = it[index]; it[index] = it[index - 1]; it[index - 1] = tmp
                                        }
                                    }
                                },
                                modifier = Modifier.size(20.dp),
                                enabled = index > 0
                            ) {
                                Icon(Icons.Default.DragHandle, null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Feedback strip ───────────────────────────────────────────────────────────

@Composable
private fun FeedbackStrip(
    isCorrect: Boolean,
    explanation: String?,
    sectionTitle: String
) {
    val bgColor = if (isCorrect) Color(0xFFD1FAE5) else Color(0xFFFFF3E0)
    val contentColor = if (isCorrect) Color(0xFF065F46) else Color(0xFF92400E)
    val message = if (isCorrect) {
        "أحسنت! فهمت المفهوم بشكل صحيح."
    } else {
        explanation ?: "راجع قسم \"$sectionTitle\" أعلاه وحاول مرة أخرى."
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                lineHeight = 20.sp
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun parseJsonOptions(raw: String): List<String> {
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (e: Exception) {
        raw.split("\n").filter { it.isNotBlank() }.map { it.trim() }
    }
}