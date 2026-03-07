package com.zeros.basheer.feature.practice.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
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
 * Card for Order questions.
 *
 * Interaction model: tap an item to select it (it gets a highlighted ring),
 * then tap another item to swap them. Tap the same item again to deselect.
 * This avoids scroll-vs-drag gesture conflicts in a scrollable Column.
 */
@Composable
fun OrderCard(
    question: Question,
    interactionState: QuestionInteractionState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember(question) {
        parseOrderItems(question.options ?: "")
    }

    var orderedItems by remember { mutableStateOf(items.shuffled()) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
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
                text = if (selectedIndex < 0) "اضغط على عنصر لتحديده، ثم اضغط على موضعه الصحيح"
                else "الآن اضغط على العنصر الذي تريد المبادلة معه",
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedIndex < 0) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            when (interactionState) {
                is QuestionInteractionState.Idle,
                is QuestionInteractionState.Interacting -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        orderedItems.forEachIndexed { index, item ->
                            OrderItem(
                                item = item,
                                position = index + 1,
                                isSelected = selectedIndex == index,
                                onClick = {
                                    when {
                                        // Tap selected item → deselect
                                        selectedIndex == index -> selectedIndex = -1
                                        // Nothing selected → select this one
                                        selectedIndex < 0 -> selectedIndex = index
                                        // Another item was selected → swap
                                        else -> {
                                            orderedItems = orderedItems.toMutableList().apply {
                                                val tmp = this[selectedIndex]
                                                this[selectedIndex] = this[index]
                                                this[index] = tmp
                                            }
                                            selectedIndex = -1
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onAnswer(orderedItems.joinToString(",")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تحقق من الإجابة")
                    }
                }

                is QuestionInteractionState.Answered -> {
                    val correctOrder = parseOrderItems(question.correctAnswer)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        correctOrder.forEachIndexed { index, item ->
                            OrderItemDisplay(item = item, position = index + 1, isCorrect = true)
                        }
                    }

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

                    Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                        Text("متابعة")
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItem(
    item: String,
    position: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 0.dp,
        label = "elevation"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        label = "containerColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        label = "borderColor"
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$position",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = item,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )

            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun OrderItemDisplay(
    item: String,
    position: Int,
    isCorrect: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) Color(0xFF4CAF50).copy(alpha = 0.1f)
            else Color(0xFFF44336).copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isCorrect) Color(0xFF4CAF50).copy(alpha = 0.2f)
                else Color(0xFFF44336).copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$position",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }

            Text(
                text = item,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Serializable
data class OrderItems(val items: List<String>)

private fun parseOrderItems(json: String): List<String> {
    return try {
        Json.decodeFromString<List<String>>(json)
    } catch (e: Exception) {
        try {
            Json.decodeFromString<OrderItems>(json).items
        } catch (e2: Exception) {
            json.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
}
