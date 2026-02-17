package com.zeros.basheer.feature.quizbank.presentation.exam

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.feature.practice.presentation.components.*
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import com.zeros.basheer.feature.quizbank.presentation.exam.components.ExamNavigationSheet
import com.zeros.basheer.feature.quizbank.presentation.exam.components.ExamTimer

/**
 * Main exam session screen.
 * Shows timer, current question, and navigation controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamSessionScreen(
    onNavigateBack: () -> Unit,
    onExamComplete: (Long) -> Unit,
    viewModel: ExamSessionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Navigate to results when complete
    if (state.isComplete) {
        state.attemptId?.let { attemptId ->
            LaunchedEffect(Unit) {
                onExamComplete(attemptId)
            }
        }
        return
    }

    // Navigation sheet
    if (state.showNavigationSheet) {
        ExamNavigationSheet(
            sections = state.sections,
            questions = state.questions,
            currentQuestionIndex = state.currentQuestionIndex,
            answeredQuestions = state.answers.keys,
            flaggedQuestions = state.flaggedQuestions,
            onQuestionClick = { index ->
                viewModel.onEvent(ExamSessionEvent.NavigateToQuestion(index))
            },
            onDismiss = {
                viewModel.onEvent(ExamSessionEvent.ToggleNavigationSheet)
            }
        )
    }

    // Submit confirmation dialog
    if (state.showSubmitDialog) {
        SubmitConfirmationDialog(
            answeredCount = state.answeredCount,
            unansweredCount = state.unansweredCount,
            onConfirm = {
                viewModel.onEvent(ExamSessionEvent.ConfirmSubmit)
            },
            onDismiss = {
                viewModel.onEvent(ExamSessionEvent.CancelSubmit)
            }
        )
    }

    Scaffold(
        topBar = {
            ExamTopBar(
                examTitle = state.exam?.titleAr ?: "",
                timeRemainingSeconds = state.timeRemainingSeconds,
                onNavigationClick = {
                    viewModel.onEvent(ExamSessionEvent.ToggleNavigationSheet)
                },
                onSubmitClick = {
                    viewModel.onEvent(ExamSessionEvent.ShowSubmitDialog)
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    ErrorMessage(
                        message = state.error!!,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.currentQuestion != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Progress bar
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )

                        // Section header
                        state.currentSection?.let { section ->
                            SectionHeader(
                                title = section.title,
                                questionNumber = state.currentQuestionIndex + 1,
                                totalQuestions = state.questions.size
                            )
                        }

                        // Question card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            QuestionCard(
                                question = state.currentQuestion!!,
                                interactionState = state.interactionState,
                                onAnswer = { answer ->
                                    viewModel.onEvent(ExamSessionEvent.AnswerQuestion(answer))
                                },
                                onContinue = {
                                    viewModel.onEvent(ExamSessionEvent.ContinueToNext)
                                }
                            )
                        }

                        // Navigation controls
                        ExamNavigationControls(
                            canGoPrevious = state.currentQuestionIndex > 0,
                            canGoNext = !state.isLastQuestion,
                            isFlagged = state.isQuestionFlagged(state.currentQuestion!!.id),
                            showContinue = state.interactionState is QuestionInteractionState.Answered,
                            onPrevious = {
                                viewModel.onEvent(ExamSessionEvent.PreviousQuestion)
                            },
                            onNext = {
                                viewModel.onEvent(ExamSessionEvent.ContinueToNext)
                            },
                            onToggleFlag = {
                                viewModel.onEvent(ExamSessionEvent.ToggleFlagCurrentQuestion)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamTopBar(
    examTitle: String,
    timeRemainingSeconds: Int,
    onNavigationClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = examTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                ExamTimer(timeRemainingSeconds = timeRemainingSeconds)
            }
        },
        actions = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "التنقل"
                )
            }

            TextButton(onClick = onSubmitClick) {
                Text("تسليم")
            }
        }
    )
}

@Composable
private fun SectionHeader(
    title: String,
    questionNumber: Int,
    totalQuestions: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "السؤال $questionNumber من $totalQuestions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExamNavigationControls(
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    isFlagged: Boolean,
    showContinue: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleFlag: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            OutlinedButton(
                onClick = onPrevious,
                enabled = canGoPrevious,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("السابق")
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Flag button
            IconButton(
                onClick = onToggleFlag,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isFlagged) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "تعليم السؤال",
                    tint = if (isFlagged) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Next/Continue button
            Button(
                onClick = onNext,
                enabled = canGoNext || showContinue,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (showContinue) "متابعة" else "التالي")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: com.zeros.basheer.feature.quizbank.domain.model.Question,
    interactionState: QuestionInteractionState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit
) {
    when (question.type) {
        QuestionType.TRUE_FALSE -> {
            TrueFalseCard(
                question = question,
                interactionState = interactionState,
                onAnswer = onAnswer,
                onContinue = onContinue
            )
        }

        QuestionType.MCQ -> {
            McqCard(
                question = question,
                interactionState = interactionState,
                onAnswer = onAnswer,
                onContinue = onContinue
            )
        }

        QuestionType.FILL_BLANK -> {
            FillBlankCard(
                question = question,
                interactionState = interactionState,
                onAnswer = onAnswer,
                onContinue = onContinue
            )
        }

        QuestionType.SHORT_ANSWER,
        QuestionType.EXPLAIN,
        QuestionType.LIST -> {
            OpenAnswerCard(
                question = question,
                interactionState = interactionState,
                onAnswer = onAnswer,
                onContinue = onContinue
            )
        }

        QuestionType.FIGURE -> {
            FigureCard(
                question = question,
                interactionState = interactionState,
                onAnswer = onAnswer,
                onContinue = onContinue
            )
        }

        QuestionType.COMPARE -> {
            CompareCard(
                question = question,
                interactionState = interactionState,
                onAnswer = onAnswer,
                onContinue = onContinue
            )
        }

        QuestionType.TABLE -> {
            TableCard(
                question = question,
                interactionState = interactionState,
                onAnswer = onAnswer,
                onContinue = onContinue
            )
        }

        QuestionType.ORDER -> {
            OrderCard(
                question = question,
                interactionState = interactionState,
                onAnswer = onAnswer,
                onContinue = onContinue
            )
        }

        QuestionType.MATCH -> {
            MatchCard(
                question = question,
                interactionState = interactionState,
                onAnswer = onAnswer,
                onContinue = onContinue
            )
        }
    }
}

@Composable
private fun SubmitConfirmationDialog(
    answeredCount: Int,
    unansweredCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسليم الامتحان؟") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("هل أنت متأكد من تسليم الامتحان؟")
                Text(
                    text = "الأسئلة المجابة: $answeredCount",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (unansweredCount > 0) {
                    Text(
                        text = "الأسئلة غير المجابة: $unansweredCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("تسليم")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
    }
}