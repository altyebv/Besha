package com.zeros.basheer.ui.screens.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zeros.basheer.feature.practice.domain.model.PracticeGenerationType
import com.zeros.basheer.feature.practice.domain.model.PracticeSession

/**
 * Session Result Screen - Shows final results after completing practice
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionResultScreen(
    session: PracticeSession,
    correctCount: Int,
    wrongCount: Int,
    skippedCount: Int,
    onExit: () -> Unit,
    onRetry: () -> Unit
) {
    val totalAnswered = correctCount + wrongCount
    val score = if (totalAnswered > 0) {
        (correctCount.toFloat() / totalAnswered * 100).toInt()
    } else 0

    val scoreColor = when {
        score >= 80 -> Color(0xFF4CAF50) // Green
        score >= 60 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }

    val performanceMessage = when {
        score >= 90 -> "ممتاز! 🎉"
        score >= 80 -> "جيد جداً! 👏"
        score >= 70 -> "جيد 👍"
        score >= 60 -> "مقبول"
        else -> "يحتاج تحسين"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "النتيجة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Score Circle
            ScoreCircle(
                score = score,
                color = scoreColor
            )

            // Performance message
            Text(
                text = performanceMessage,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )

            // Stats cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Correct
                StatCard(
                    label = "صحيحة",
                    value = correctCount.toString(),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )

                // Wrong
                StatCard(
                    label = "خاطئة",
                    value = wrongCount.toString(),
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )

                // Skipped
                StatCard(
                    label = "متخطاة",
                    value = skippedCount.toString(),
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.weight(1f)
                )
            }

            // Session info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoRow(
                        icon = Icons.Default.Category,
                        label = "نوع التدريب",
                        value = session.generationType.toArabic()
                    )

                    InfoRow(
                        icon = Icons.Default.Quiz,
                        label = "عدد الأسئلة",
                        value = "${session.questionCount} سؤال"
                    )

                    if (session.totalTimeSeconds != null) {
                        InfoRow(
                            icon = Icons.Default.Timer,
                            label = "الوقت المستغرق",
                            value = formatTime(session.totalTimeSeconds)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Retry button
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إعادة المحاولة")
                }

                // Exit button
                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("العودة للقائمة")
                }
            }
        }
    }
}

/**
 * Score circle display
 */
@Composable
private fun ScoreCircle(
    score: Int,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(160.dp)
            .background(color.copy(alpha = 0.1f), CircleShape)
            .padding(12.dp)
            .background(color.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "النسبة",
                style = MaterialTheme.typography.bodyMedium,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Stat card component
 */
@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
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
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Info row component
 */
@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Format time in seconds to readable format
 */
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        "$minutes د $remainingSeconds ث"
    } else {
        "$remainingSeconds ث"
    }
}

/**
 * Convert PracticeGenerationType to Arabic
 */
private fun PracticeGenerationType.toArabic(): String {
    return when (this) {
        PracticeGenerationType.FULL_EXAM -> "امتحان كامل"
        PracticeGenerationType.BY_UNIT -> "حسب الوحدة"
        PracticeGenerationType.BY_LESSON -> "حسب الدرس"
        PracticeGenerationType.BY_CONCEPT -> "حسب المفهوم"
        PracticeGenerationType.BY_PROGRESS -> "حسب التقدم"
        PracticeGenerationType.WEAK_AREAS -> "نقاط الضعف"
        PracticeGenerationType.QUICK_REVIEW -> "مراجعة سريعة"
        PracticeGenerationType.BY_TYPE -> "حسب النوع"
        PracticeGenerationType.BY_SOURCE -> "حسب المصدر"
        PracticeGenerationType.CUSTOM -> "مخصص"
    }
}