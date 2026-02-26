package com.zeros.basheer.feature.quizbank.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.feature.practice.domain.model.PracticeSession
import com.zeros.basheer.feature.quizbank.presentation.foundation.*

@Composable
internal fun RecentSessionsStrip(
    sessions: List<PracticeSession>,
    onSessionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "آخر الجلسات",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = textSecondary
        )
        sessions.forEach { session ->
            RecentSessionRow(
                session = session,
                onClick = { onSessionClick(session.id) }
            )
        }
    }
}

@Composable
private fun RecentSessionRow(session: PracticeSession, onClick: () -> Unit) {
    val score = session.score
    val color = if (score != null) scoreColor(score) else textMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgCard)
            .border(1.dp, bgBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = practiceTypeLabel(session.generationType),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary
            )
            Text(
                text = "${session.questionCount} سؤال",
                style = MaterialTheme.typography.bodySmall,
                color = textPrimary
            )
        }

        if (score != null) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f))
                    .border(1.dp, color.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${score.toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}