package com.zeros.basheer.ui.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Card displayed at the end of a lesson.
 * Shows completion status and actions.
 */
@Composable
fun LessonCompleteCard(
    lessonTitle: String,
    readingTimeSeconds: Long,
    isAlreadyCompleted: Boolean,
    onMarkComplete: () -> Unit,
    onNextLesson: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val readingMinutes = readingTimeSeconds / 60
    val readingSecondsRemainder = readingTimeSeconds % 60
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success icon
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            
            // Title
            Text(
                text = if (isAlreadyCompleted) "تمت المراجعة" else "أحسنت!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Lesson name
            Text(
                text = "أكملت درس \"$lessonTitle\"",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            // Reading time
            if (readingTimeSeconds > 0) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = buildString {
                            append("وقت القراءة: ")
                            if (readingMinutes > 0) {
                                append("$readingMinutes دقيقة")
                                if (readingSecondsRemainder > 0) {
                                    append(" و $readingSecondsRemainder ثانية")
                                }
                            } else {
                                append("$readingSecondsRemainder ثانية")
                            }
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Actions
            if (!isAlreadyCompleted) {
                Button(
                    onClick = onMarkComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تحديد كمكتمل")
                }
            }
            
            onNextLesson?.let { next ->
                OutlinedButton(
                    onClick = next,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("الدرس التالي")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Outlined.NavigateNext,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
