package com.zeros.basheer.feature.quizbank.presentation.exam

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.model.QuestionResponse
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Shows detailed exam results.
 * - Overall score
 * - Section breakdown
 * - Question-by-question results
 * - Concepts covered
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultScreen(
    attemptId: Long,
    onExit: () -> Unit,
    onRetry: () -> Unit
) {
    var state by remember { mutableStateOf<ResultState>(ResultState.Loading) }

    LaunchedEffect(attemptId) {
        // TODO: Load results from repository
        // For now, placeholder
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("نتيجة الامتحان") },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state) {
                ResultState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ResultState.Error -> {
                    ErrorDisplay(
                        message = (state as ResultState.Error).message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ResultState.Success -> {
                    val data = (state as ResultState.Success)
                    ResultContent(
                        score = data.score,
                        totalPoints = data.totalPoints,
                        percentage = data.percentage,
                        correctCount = data.correctCount,
                        wrongCount = data.wrongCount,
                        timeSpent = data.timeSpent,
                        onExit = onExit,
                        onRetry = onRetry
                    )
                }
            }
        }
    }
}

sealed class ResultState {
    object Loading : ResultState()
    data class Error(val message: String) : ResultState()
    data class Success(
        val score: Int,
        val totalPoints: Int,
        val percentage: Float,
        val correctCount: Int,
        val wrongCount: Int,
        val timeSpent: Int
    ) : ResultState()
}

@Composable
private fun ResultContent(
    score: Int,
    totalPoints: Int,
    percentage: Float,
    correctCount: Int,
    wrongCount: Int,
    timeSpent: Int,
    onExit: () -> Unit,
    onRetry: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Score card
        item {
            ScoreCard(
                score = score,
                totalPoints = totalPoints,
                percentage = percentage
            )
        }

        // Stats
        item {
            StatsRow(
                correctCount = correctCount,
                wrongCount = wrongCount,
                timeSpent = timeSpent
            )
        }

        // Section breakdown placeholder
        item {
            Text(
                text = "تفصيل الأقسام",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "سيتم عرض التفاصيل الكاملة قريباً",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إعادة المحاولة")
                }

                Button(
                    onClick = onExit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("إنهاء")
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(
    score: Int,
    totalPoints: Int,
    percentage: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                percentage >= 90 -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                percentage >= 75 -> Color(0xFF2196F3).copy(alpha = 0.2f)
                percentage >= 60 -> Color(0xFFFFC107).copy(alpha = 0.2f)
                else -> Color(0xFFF44336).copy(alpha = 0.2f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "النتيجة",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "$score / $totalPoints",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    percentage >= 90 -> Color(0xFF4CAF50)
                    percentage >= 75 -> Color(0xFF2196F3)
                    percentage >= 60 -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                }
            )

            Text(
                text = String.format("%.1f%%", percentage),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = when {
                    percentage >= 90 -> "ممتاز!"
                    percentage >= 75 -> "جيد جداً"
                    percentage >= 60 -> "جيد"
                    percentage >= 50 -> "مقبول"
                    else -> "يحتاج تحسين"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsRow(
    correctCount: Int,
    wrongCount: Int,
    timeSpent: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            icon = Icons.Default.Check,
            label = "صحيح",
            value = correctCount.toString(),
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            icon = Icons.Default.Close,
            label = "خطأ",
            value = wrongCount.toString(),
            color = Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            icon = Icons.Default.Timer,
            label = "الوقت",
            value = formatTime(timeSpent),
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorDisplay(
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

private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) {
        "${hours}س ${minutes}د"
    } else {
        "${minutes}د"
    }
}