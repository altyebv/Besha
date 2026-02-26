package com.zeros.basheer.feature.quizbank.presentation.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.quizbank.domain.model.Exam
import com.zeros.basheer.feature.quizbank.domain.model.ExamType
import com.zeros.basheer.feature.quizbank.presentation.foundation.*

@Composable
internal fun ExamCard(
    exam: Exam,
    score: Float? = null,   // null = never attempted; wire to attempt history when ready
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgCard)
            .border(1.dp, bgBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Type icon box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentExam.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (exam.examType) {
                        ExamType.FINAL      -> "🏆"
                        ExamType.SEMI_FINAL -> "📝"
                        ExamType.MONTHLY    -> "🗓"
                        null                -> "📋"
                    },
                    fontSize = 22.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = exam.titleAr,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (exam.year != null)
                        MetaChip(text = "${exam.year}", icon = Icons.Outlined.CalendarToday)
                    if (exam.duration != null)
                        MetaChip(text = "${exam.duration}د", icon = Icons.Outlined.Timer)
                    if (exam.totalPoints != null)
                        MetaChip(text = "${exam.totalPoints}ن", icon = Icons.Outlined.EmojiEvents)
                }

                if (exam.schoolName != null) {
                    Text(
                        text = exam.schoolName,
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentExam.copy(alpha = 0.70f)
                    )
                }
            }

            AttemptStatusBadge(score = score)
        }
    }
}

@Composable
private fun MetaChip(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textMuted,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textSecondary
        )
    }
}

@Composable
private fun AttemptStatusBadge(score: Float?) {
    if (score == null) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(bgBorder)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "لم تُؤدَّ",
                style = MaterialTheme.typography.labelSmall,
                color = textMuted
            )
        }
    } else {
        val color = scoreColor(score)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.18f))
                .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${score.toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}