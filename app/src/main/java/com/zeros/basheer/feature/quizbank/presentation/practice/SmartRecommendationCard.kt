package com.zeros.basheer.feature.quizbank.presentation.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.quizbank.presentation.foundation.*

@Composable
internal fun SmartRecommendationCard(
    averageScore: Float?,
    onStart: () -> Unit
) {
    val weaknessNote = when {
        averageScore == null -> "ابدأ جلستك الأولى لتحديد نقاط ضعفك"
        averageScore < 50f   -> "دقتك منخفضة — ركّز على المفاهيم الأساسية"
        averageScore < 75f   -> "أداء جيد — تعمّق في نقاط ضعفك الحالية"
        else                 -> "ممتاز — حافظ على مستواك بمراجعة دورية"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        AccentExam.copy(alpha = 0.22f),
                        AccentExam.copy(alpha = 0.10f)
                    )
                )
            )
            .border(1.dp, AccentExam.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🎯", fontSize = 24.sp)
                Column {
                    Text(
                        text = "جلسة ذكية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentExam
                    )
                    Text(
                        text = weaknessNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SmartTag("٢٠ سؤال")
                    SmartTag("نقاط ضعف")
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentExam)
                        .clickable(onClick = onStart)
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "ابدأ الآن",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = bgCard
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AccentExam.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = AccentExam
        )
    }
}