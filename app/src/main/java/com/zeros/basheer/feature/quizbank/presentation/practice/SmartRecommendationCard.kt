package com.zeros.basheer.feature.quizbank.presentation.practice

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.quizbank.presentation.foundation.*

/**
 * Smart session card — adapts to the student's actual performance state.
 *
 * States (in priority order):
 * 1. [isLoading] = true → loading indicator (weak stats query in flight)
 * 2. [weakAreaCount] > 0 → primary state: shows count badge + score context, CTA enabled
 * 3. [averageScore] != null, [weakAreaCount] == 0 → strong performance: encourage review
 * 4. Both null/zero → new user: prompt to start first session
 *
 * The "ابدأ الآن" button is always enabled (even in empty state) so the student can
 * start building signal. The VM handles the "no qualifying weak questions" error case.
 */
@Composable
internal fun SmartRecommendationCard(
    averageScore: Float?,
    weakAreaCount: Int,
    isLoading: Boolean,
    onStart: () -> Unit
) {
    val hasWeakAreas = weakAreaCount > 0
    val hasHistory   = averageScore != null

    // Accent shifts to score-derived color once there's data
    val accentColor: Color = when {
        !hasHistory         -> AccentExam
        averageScore!! < 50f -> ScoreLow.copy(red = 0.95f)   // keeps it warm, not alarming
        averageScore < 75f  -> AccentExam
        else                -> ScoreHigh
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        accentColor.copy(alpha = 0.18f),
                        accentColor.copy(alpha = 0.07f)
                    )
                )
            )
            .border(1.dp, accentColor.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // ── Header row ────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        !hasHistory  -> "🧭"
                        hasWeakAreas -> "🎯"
                        else         -> "✅"
                    },
                    fontSize = 24.sp
                )
                Column {
                    Text(
                        text = "جلسة ذكية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = when {
                            !hasHistory  -> "ابدأ جلستك الأولى لتحديد نقاط ضعفك"
                            hasWeakAreas && averageScore!! < 50f ->
                                "دقتك منخفضة — ركّز على المفاهيم الأساسية"
                            hasWeakAreas ->
                                "أداء جيد — تعمّق في نقاط ضعفك الحالية"
                            else         ->
                                "ممتاز — حافظ على مستواك بمراجعة دورية"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary
                    )
                }
            }

            // ── Stats row (score + weak count) ────────────────────────────
            if (hasHistory) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Average score chip
                    StatChip(
                        label = "متوسط الدقة",
                        value = "${averageScore!!.toInt()}٪",
                        valueColor = scoreColor(averageScore)
                    )
                    // Weak area count chip — animated so it pops when data arrives
                    AnimatedContent(
                        targetState = weakAreaCount,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(150))
                        },
                        label = "weak_count"
                    ) { count ->
                        if (count > 0) {
                            StatChip(
                                label = "سؤال ضعيف",
                                value = "$count",
                                valueColor = if (count >= 10) ScoreLow else AccentExam
                            )
                        }
                    }
                }
            }

            // ── Action row ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tags: question count changes based on weak area availability
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SmartTag(if (hasWeakAreas) "حتى ٢٠ سؤال" else "جلسة تمهيدية")
                    SmartTag("نقاط ضعف")
                }

                // CTA button — shows spinner while session is being created
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor)
                        .clickable(enabled = !isLoading, onClick = onStart)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isLoading,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                        },
                        label = "cta_state"
                    ) { loading ->
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = bgCard,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (hasWeakAreas) "ابدأ الجلسة" else "ابدأ الآن",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = bgCard
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────────

@Composable
private fun StatChip(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
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
private fun SmartTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AccentExam.copy(alpha = 0.13f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = AccentExam
        )
    }
}