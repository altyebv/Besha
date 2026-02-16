package com.zeros.basheer.feature.practice.presentation.components

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
 * Card for Table questions.
 * User fills in empty cells of a table.
 * tableData format: JSON with headers, rows, and which cells are editable
 */
@Composable
fun TableCard(
    question: Question,
    interactionState: QuestionInteractionState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tableData = remember(question) {
        parseTableData(question.tableData ?: question.options ?: "")
    }

    // Initialize user inputs for editable cells
    var userInputs by remember {
        mutableStateOf(
            tableData.rows.mapIndexed { rowIndex, row ->
                row.cells.mapIndexed { colIndex, cell ->
                    if (tableData.editableCells.contains(rowIndex to colIndex)) "" else cell
                }
            }
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

            when (interactionState) {
                is QuestionInteractionState.Idle,
                is QuestionInteractionState.Interacting -> {
                    // Table with editable cells
                    TableDisplay(
                        headers = tableData.headers,
                        rows = userInputs,
                        editableCells = tableData.editableCells,
                        onCellChange = { rowIndex, colIndex, newValue ->
                            userInputs = userInputs.toMutableList().also { newRows ->
                                newRows[rowIndex] = newRows[rowIndex].toMutableList().also { newCells ->
                                    newCells[colIndex] = newValue
                                }
                            }
                        },
                        isAnswered = false
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Submit button
                    val allFilled = tableData.editableCells.all { (rowIndex, colIndex) ->
                        userInputs.getOrNull(rowIndex)?.getOrNull(colIndex)?.isNotBlank() == true
                    }

                    Button(
                        onClick = {
                            // Format answer as JSON
                            val answer = Json.encodeToString(
                                TableAnswer.serializer(),
                                TableAnswer(userInputs)
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
                    val correctAnswers = parseTableAnswer(question.correctAnswer)

                    TableDisplay(
                        headers = tableData.headers,
                        rows = correctAnswers.ifEmpty { tableData.rows.map { it.cells } },
                        editableCells = tableData.editableCells,
                        onCellChange = { _, _, _ -> },
                        isAnswered = true
                    )

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
private fun TableDisplay(
    headers: List<String>,
    rows: List<List<String>>,
    editableCells: Set<Pair<Int, Int>>,
    onCellChange: (Int, Int, String) -> Unit,
    isAnswered: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column {
            // Headers
            if (headers.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    headers.forEach { header ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = header,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (header != headers.last()) {
                            VerticalDivider()
                        }
                    }
                }
                HorizontalDivider()
            }

            // Rows
            rows.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEachIndexed { colIndex, cell ->
                        val isEditable = editableCells.contains(rowIndex to colIndex)

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isEditable && !isAnswered) {
                                OutlinedTextField(
                                    value = cell,
                                    onValueChange = { onCellChange(rowIndex, colIndex, it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        textAlign = TextAlign.Center
                                    ),
                                    singleLine = true
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = if (isAnswered && isEditable) {
                                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    } else {
                                        Color.Transparent
                                    }
                                ) {
                                    Text(
                                        text = cell,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (isAnswered && isEditable) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        }
                                    )
                                }
                            }
                        }

                        if (colIndex != row.lastIndex) {
                            VerticalDivider()
                        }
                    }
                }

                if (rowIndex != rows.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .padding(vertical = 4.dp),
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

// Data structures
@Serializable
data class TableData(
    val headers: List<String> = emptyList(),
    val rows: List<TableRow>,
    val editableCells: Set<Pair<Int, Int>> = emptySet()
)

@Serializable
data class TableRow(
    val cells: List<String>
)

@Serializable
data class TableAnswer(
    val rows: List<List<String>>
)

// Parsing helpers
private fun parseTableData(json: String): TableData {
    return try {
        Json.decodeFromString<TableData>(json)
    } catch (e: Exception) {
        // Fallback: simple 2x2 table
        TableData(
            headers = listOf("العمود 1", "العمود 2"),
            rows = listOf(
                TableRow(listOf("", "")),
                TableRow(listOf("", ""))
            ),
            editableCells = setOf(0 to 0, 0 to 1, 1 to 0, 1 to 1)
        )
    }
}

private fun parseTableAnswer(json: String): List<List<String>> {
    return try {
        val answer = Json.decodeFromString<TableAnswer>(json)
        answer.rows
    } catch (e: Exception) {
        emptyList()
    }
}