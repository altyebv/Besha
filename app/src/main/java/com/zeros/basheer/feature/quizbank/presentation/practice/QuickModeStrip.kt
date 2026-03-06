package com.zeros.basheer.feature.quizbank.presentation.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.core.ui.theme.SubjectChemistry
import com.zeros.basheer.core.ui.theme.SubjectArabic
import com.zeros.basheer.core.ui.theme.SubjectGeography
import com.zeros.basheer.feature.practice.domain.model.PracticeGenerationType
import com.zeros.basheer.feature.quizbank.presentation.foundation.*

/**
 * Quick-mode chips row.
 *
 * - QUICK_REVIEW fires directly (no filter params needed — picks random questions).
 * - WEAK_AREAS fires directly (GetWeakAreaQuestionsUseCase handles it via [onStartWeakArea]).
 * - BY_UNIT / BY_CONCEPT / BY_TYPE open the Practice Builder screen so the student
 *   can select which unit/concept/type before starting. Firing these without filters
 *   would produce an unscoped session identical to QUICK_REVIEW.
 */
@Composable
internal fun QuickModeStrip(
    onStartPractice: (PracticeGenerationType) -> Unit,
    onOpenBuilder: (PracticeGenerationType) -> Unit,
    onStartWeakArea: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "جلسات سريعة",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = textSecondary
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Direct-start chips (no filters needed)
            QuickModeChip("⚡", "مراجعة سريعة",  AccentPractice,   onClick = { onStartPractice(PracticeGenerationType.QUICK_REVIEW) })
            QuickModeChip("📉", "نقاط الضعف",    ScoreLow,          onClick = onStartWeakArea)

            // Builder chips — need filter selection before starting
            QuickModeChip("🗂", "حسب الوحدة",    SubjectGeography,  onClick = { onOpenBuilder(PracticeGenerationType.BY_UNIT) })
            QuickModeChip("💡", "حسب المفهوم",   SubjectChemistry,  onClick = { onOpenBuilder(PracticeGenerationType.BY_CONCEPT) })
            QuickModeChip("🔤", "حسب النوع",     SubjectArabic,     onClick = { onOpenBuilder(PracticeGenerationType.BY_TYPE) })
        }
    }
}

@Composable
private fun QuickModeChip(
    emoji: String,
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}