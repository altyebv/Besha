package com.zeros.basheer.feature.quizbank.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.feature.quizbank.domain.model.QuestionCounts
import com.zeros.basheer.feature.quizbank.presentation.foundation.*

@Composable
internal fun PulseStrip(
    averageScore: Float?,
    questionCounts: QuestionCounts?,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgCard)
            .border(1.dp, bgBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PulseStatItem(
            value = if (averageScore != null) "${averageScore.toInt()}%" else "–",
            label = "متوسط الدقة",
            valueColor = if (averageScore != null) scoreColor(averageScore) else textMuted
        )
        PulseStatDivider()
        PulseStatItem(
            value = "${questionCounts?.total ?: 0}",
            label = "سؤال متاح",
            valueColor = AccentPractice
        )
        PulseStatDivider()
        PulseStatItem(
            value = "$completedCount",
            label = "جلسة مكتملة",
            valueColor = AccentExam
        )
    }
}

@Composable
private fun PulseStatItem(value: String, label: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textSecondary
        )
    }
}

@Composable
private fun PulseStatDivider() {
    Box(
        modifier = Modifier
            .height(36.dp)
            .width(1.dp)
            .background(bgBorder)
    )
}