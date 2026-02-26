package com.zeros.basheer.feature.quizbank.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.feature.practice.domain.model.PracticeSession
import com.zeros.basheer.feature.quizbank.presentation.foundation.*

@Composable
internal fun ActiveSessionBanner(
    session: PracticeSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (session.questionCount > 0)
        session.currentQuestionIndex.toFloat() / session.questionCount else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        AccentPractice.copy(alpha = 0.20f),
                        AccentPractice.copy(alpha = 0.10f)
                    )
                )
            )
            .border(1.dp, AccentPractice.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentPractice)
                    )
                    Text(
                        text = "جلسة جارية",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentPractice,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AccentPractice.copy(alpha = 0.20f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "متابعة",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentPractice,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = AccentPractice,
                trackColor = AccentPractice.copy(alpha = 0.18f),
                strokeCap = StrokeCap.Round
            )

            Text(
                text = "السؤال ${session.currentQuestionIndex + 1} من ${session.questionCount}  ·  ${session.correctCount} صحيحة",
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )
        }
    }
}