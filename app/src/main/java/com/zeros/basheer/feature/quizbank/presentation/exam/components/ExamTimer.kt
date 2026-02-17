package com.zeros.basheer.feature.quizbank.presentation.exam.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Timer display for exam.
 * Shows warning colors when time is running low.
 */
@Composable
fun ExamTimer(
    timeRemainingSeconds: Int,
    modifier: Modifier = Modifier
) {
    val hours = timeRemainingSeconds / 3600
    val minutes = (timeRemainingSeconds % 3600) / 60
    val seconds = timeRemainingSeconds % 60

    val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    // Color based on time remaining
    val timerColor by animateColorAsState(
        targetValue = when {
            timeRemainingSeconds <= 300 -> Color(0xFFF44336)  // Red - 5 min or less
            timeRemainingSeconds <= 600 -> Color(0xFFFF9800)  // Orange - 10 min or less
            timeRemainingSeconds <= 1800 -> Color(0xFFFFC107) // Yellow - 30 min or less
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "timerColor"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = "Timer",
            tint = timerColor,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = timeText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = timerColor
        )
    }
}