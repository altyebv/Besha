package com.zeros.basheer.ui.screens.practice


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.domain.model.CardInteractionState
import com.zeros.basheer.domain.model.FeedCard
import com.zeros.basheer.ui.components.feeds.McqCard
import com.zeros.basheer.ui.components.feeds.TrueFalseCard
import com.zeros.basheer.feature.feed.domain.model.FeedItemType
import com.zeros.basheer.feature.feed.domain.model.InteractionType
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType

/**
 * Practice Session Screen - Answer questions one by one
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeSessionScreen(
    onNavigateBack: () -> Unit,
    onSessionComplete: (Long) -> Unit,
    viewModel: PracticeSessionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Show results screen when complete
    if (state.isComplete) {
        SessionResultScreen(
            session = state.session!!,
            correctCount = state.correctCount,
            wrongCount = state.wrongCount,
            skippedCount = state.skippedCount,
            onExit = onNavigateBack,
            onRetry = { viewModel.onEvent(PracticeSessionEvent.RetrySession) }
        )
        return
    }

    Scaffold(
        topBar = {
            PracticeSessionTopBar(
                currentQuestion = state.currentQuestionIndex + 1,
                totalQuestions = state.questions.size,
                correctCount = state.correctCount,
                wrongCount = state.wrongCount,
                onExit = {
                    // Show exit confirmation dialog
                    // For now, just exit
                    viewModel.onEvent(PracticeSessionEvent.ExitSession)
                    onNavigateBack()
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
                        onRetry = { /* Reload */ },
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
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

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
                                    viewModel.onEvent(PracticeSessionEvent.AnswerQuestion(answer))
                                },
                                onContinue = {
                                    viewModel.onEvent(PracticeSessionEvent.ContinueToNext)
                                }
                            )
                        }

                        // Skip button (only when idle)
                        if (state.interactionState is CardInteractionState.Idle) {
                            SkipButton(
                                onSkip = { viewModel.onEvent(PracticeSessionEvent.SkipQuestion) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }
                }

                else -> {
                    Text(
                        text = "No questions available",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

/**
 * Top Bar with progress and stats
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PracticeSessionTopBar(
    currentQuestion: Int,
    totalQuestions: Int,
    correctCount: Int,
    wrongCount: Int,
    onExit: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Question counter
                Text(
                    text = "$currentQuestion / $totalQuestions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Stats
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Correct count
                    StatChip(
                        count = correctCount,
                        color = Color(0xFF4CAF50)
                    )

                    // Wrong count
                    StatChip(
                        count = wrongCount,
                        color = Color(0xFFF44336)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onExit) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "خروج"
                )
            }
        }
    )
}

/**
 * Stat chip showing count
 */
@Composable
private fun StatChip(
    count: Int,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Question card - adapts to question type
 */
@Composable
private fun QuestionCard(
    question: Question,
    interactionState: CardInteractionState,
    onAnswer: (String) -> Unit,
    onContinue: () -> Unit
) {
    // Convert Question to FeedCard format for reusing existing components
    val feedCard = question.toFeedCard()

    when (question.type) {
        QuestionType.TRUE_FALSE -> {
            TrueFalseCard(
                card = feedCard,
                interactionState = interactionState,
                onAnswer = onAnswer,
                onContinue = onContinue
            )
        }

        QuestionType.MCQ -> {
            McqCard(
                card = feedCard,
                interactionState = interactionState,
                onAnswer = onAnswer,
                onContinue = onContinue
            )
        }

        else -> {
            // For other question types, show a placeholder
            OtherQuestionTypeCard(
                question = question,
                onContinue = onContinue
            )
        }
    }
}

/**
 * Placeholder for other question types (EXPLAIN, LIST, etc.)
 */
@Composable
private fun OtherQuestionTypeCard(
    question: Question,
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = question.textAr,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "نوع السؤال: ${question.type.toArabic()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "الإجابة النموذجية:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = question.correctAnswer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("متابعة")
            }
        }
    }
}

/**
 * Skip button
 */
@Composable
private fun SkipButton(
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onSkip,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("تخطي")
    }
}

/**
 * Error message
 */
@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "حدث خطأ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(onClick = onRetry) {
            Text("إعادة المحاولة")
        }
    }
}

/**
 * Extension to convert Question to FeedCard
 */
private fun Question.toFeedCard(): FeedCard {
    return FeedCard(
        id = this.id,
        conceptId = "", // Not needed for practice
        subjectId = this.subjectId,
        subjectName = "", // Not needed
        type = FeedItemType.MINI_QUIZ,
        contentAr = this.textAr,
        contentEn = this.textEn,
        imageUrl = this.imageUrl,
        interactionType = when (this.type) {
            QuestionType.TRUE_FALSE -> InteractionType.SWIPE_TF
            QuestionType.MCQ -> InteractionType.MCQ
            else -> InteractionType.TAP_CONFIRM
        },
        correctAnswer = this.correctAnswer,
        options = this.options?.let { parseOptions(it) },
        explanation = this.explanation,
        priority = 0
    )
}

/**
 * Parse JSON options string
 */
private fun parseOptions(optionsJson: String): List<String> {
    return try {
        // Simple JSON array parsing for ["option1", "option2", ...]
        optionsJson
            .trim()
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * Convert QuestionType to Arabic
 */
private fun QuestionType.toArabic(): String {
    return when (this) {
        QuestionType.TRUE_FALSE -> "صح / خطأ"
        QuestionType.MCQ -> "اختيار من متعدد"
        QuestionType.FILL_BLANK -> "أكمل الفراغ"
        QuestionType.MATCH -> "وصّل"
        QuestionType.SHORT_ANSWER -> "أجب بإيجاز"
        QuestionType.EXPLAIN -> "اشرح / علل"
        QuestionType.LIST -> "اذكر"
        QuestionType.TABLE -> "أكمل الجدول"
        QuestionType.FIGURE -> "من الشكل"
        QuestionType.COMPARE -> "قارن بين"
        QuestionType.ORDER -> "رتب"
    }
}