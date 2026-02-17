package com.zeros.basheer.feature.quizbank.presentation.exam.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.feature.quizbank.domain.model.ExamSection
import com.zeros.basheer.feature.quizbank.domain.model.Question

/**
 * Bottom sheet showing all questions in grid format.
 * Organized by sections.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamNavigationSheet(
    sections: List<ExamSection>,
    questions: List<Question>,
    currentQuestionIndex: Int,
    answeredQuestions: Set<String>,
    flaggedQuestions: Set<String>,
    onQuestionClick: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "التنقل بين الأسئلة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(
                    color = Color(0xFF4CAF50),
                    label = "مجاب",
                    icon = { Icon(Icons.Default.Check, null, tint = Color.White) }
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.primary,
                    label = "حالي"
                )
                LegendItem(
                    color = Color(0xFFFFC107),
                    label = "مُعلّم",
                    icon = { Icon(Icons.Default.Flag, null, tint = Color.White) }
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.outline,
                    label = "غير مجاب"
                )
            }

            HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

            // Questions by section
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (sections.isEmpty()) {
                    // No sections - show all questions
                    item {
                        QuestionGrid(
                            sectionTitle = "جميع الأسئلة",
                            questions = questions,
                            startIndex = 0,
                            currentIndex = currentQuestionIndex,
                            answeredQuestions = answeredQuestions,
                            flaggedQuestions = flaggedQuestions,
                            onQuestionClick = onQuestionClick
                        )
                    }
                } else {
                    // Show by sections
                    var questionIndex = 0
                    sections.forEach { section ->
                        item {
                            val sectionQuestions = questions.filter { q ->
                                section.questionIds.contains(q.id)
                            }

                            QuestionGrid(
                                sectionTitle = section.title,
                                questions = sectionQuestions,
                                startIndex = questionIndex,
                                currentIndex = currentQuestionIndex,
                                answeredQuestions = answeredQuestions,
                                flaggedQuestions = flaggedQuestions,
                                onQuestionClick = onQuestionClick
                            )

                            questionIndex += sectionQuestions.size
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuestionGrid(
    sectionTitle: String,
    questions: List<Question>,
    startIndex: Int,
    currentIndex: Int,
    answeredQuestions: Set<String>,
    flaggedQuestions: Set<String>,
    onQuestionClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section title
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Questions grid (5 per row)
        val rows = questions.chunked(5)
        rows.forEachIndexed { rowIndex, rowQuestions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowQuestions.forEachIndexed { colIndex, question ->
                    val questionIndex = startIndex + (rowIndex * 5) + colIndex

                    QuestionCell(
                        questionNumber = questionIndex + 1,
                        isCurrent = questionIndex == currentIndex,
                        isAnswered = answeredQuestions.contains(question.id),
                        isFlagged = flaggedQuestions.contains(question.id),
                        onClick = { onQuestionClick(questionIndex) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fill empty cells in last row
                repeat(5 - rowQuestions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuestionCell(
    questionNumber: Int,
    isCurrent: Boolean,
    isAnswered: Boolean,
    isFlagged: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isAnswered -> Color(0xFF4CAF50)
        isFlagged -> Color(0xFFFFC107)
        isCurrent -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isAnswered || isCurrent || isFlagged -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = if (isCurrent) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        border = BorderStroke(
            width = if (isCurrent) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = questionNumber.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = contentColor
                )

                if (isAnswered && isFlagged) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(12.dp)
                    )
                } else if (isAnswered) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(12.dp)
                    )
                } else if (isFlagged) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    icon: @Composable (() -> Unit)? = null
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = MaterialTheme.shapes.small,
            color = color
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon?.invoke()
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}