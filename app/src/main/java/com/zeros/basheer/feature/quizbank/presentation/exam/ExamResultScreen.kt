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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Shows detailed exam results loaded from the database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultScreen(
    attemptId: Long,
    onExit: () -> Unit,
    onRetry: () -> Unit,
    viewModel: ExamResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("نتيجة الامتحان") },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.Default.Close, "إغلاق")
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
            when (val state = uiState) {
                ExamResultUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ExamResultUiState.Error -> {
                    ErrorDisplay(
                        message = state.message,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ExamResultUiState.Success -> {
                    ResultContent(
                        data = state.data,
                        onExit = onExit,
                        onRetry = onRetry
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultContent(
    data: ExamResultData,
    onExit: () -> Unit,
    onRetry: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status banner (only for non-normal completions)
        item { ExamStatusBanner(attempt = data.attempt) }

        item { ScoreCard(score = data.score, totalPoints = data.totalPoints, percentage = data.percentage) }

        item {
            StatsRow(
                correctCount = data.correctCount,
                wrongCount = data.wrongCount,
                unansweredCount = data.unansweredCount,
                timeSpentSeconds = data.timeSpentSeconds
            )
        }

        if (data.sectionResults.isNotEmpty()) {
            item {
                Text(
                    text = "تفصيل الأقسام",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(data.sectionResults) { section ->
                SectionResultCard(section = section)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onRetry, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إعادة المحاولة")
                }
                Button(onClick = onExit, modifier = Modifier.weight(1f)) {
                    Text("إنهاء")
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(score: Int, totalPoints: Int, percentage: Float) {
    val color = when {
        percentage >= 90 -> Color(0xFF4CAF50)
        percentage >= 75 -> Color(0xFF2196F3)
        percentage >= 60 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("النتيجة", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$score / $totalPoints", style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold, color = color)
            Text(String.format("%.1f%%", percentage), style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = color)
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
private fun StatsRow(correctCount: Int, wrongCount: Int, unansweredCount: Int, timeSpentSeconds: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard(icon = Icons.Default.Check, label = "صحيح", value = correctCount.toString(),
            color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
        StatCard(icon = Icons.Default.Close, label = "خطأ", value = wrongCount.toString(),
            color = Color(0xFFF44336), modifier = Modifier.weight(1f))
        if (unansweredCount > 0) {
            StatCard(icon = Icons.Default.HelpOutline, label = "بلا إجابة",
                value = unansweredCount.toString(), color = Color(0xFF9E9E9E), modifier = Modifier.weight(1f))
        } else {
            StatCard(icon = Icons.Default.Timer, label = "الوقت", value = formatTime(timeSpentSeconds),
                color = Color(0xFF2196F3), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SectionResultCard(section: SectionResult) {
    val pct = if (section.totalPoints > 0) section.score.toFloat() / section.totalPoints else 0f
    val color = when {
        pct >= 0.75f -> Color(0xFF4CAF50)
        pct >= 0.50f -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(section.title, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("${section.score} / ${section.totalPoints}", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = color)
            }
            LinearProgressIndicator(
                progress = { pct },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )
            Text("${section.correctCount} صحيح من ${section.totalCount} سؤال",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatCard(icon: ImageVector, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ErrorDisplay(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry) { Text("إعادة المحاولة") }
    }
}

@Composable
private fun ExamStatusBanner(attempt: com.zeros.basheer.feature.quizbank.domain.model.QuizAttempt) {
    when (attempt.status) {
        com.zeros.basheer.feature.quizbank.domain.model.ExamAttemptStatus.DISQUALIFIED -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Block, null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp))
                    Column {
                        Text("تم إيقاف الامتحان",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("غادرت التطبيق أثناء الامتحان",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                    }
                }
            }
        }
        com.zeros.basheer.feature.quizbank.domain.model.ExamAttemptStatus.TIME_EXPIRED -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF6F00).copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Timer, null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(24.dp))
                    Column {
                        Text("انتهى الوقت",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100))
                        Text("تم التسليم تلقائياً عند انتهاء المؤقت",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100).copy(alpha = 0.8f))
                    }
                }
            }
        }
        else -> { /* Normal completion — no banner */ }
    }
}

private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hours > 0 -> "${hours}س ${minutes}د"
        minutes > 0 -> "${minutes}د ${secs}ث"
        else -> "${secs}ث"
    }
}