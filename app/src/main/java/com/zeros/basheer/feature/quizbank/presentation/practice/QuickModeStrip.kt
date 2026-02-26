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

@Composable
internal fun QuickModeStrip(
    onStartPractice: (PracticeGenerationType) -> Unit
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
            QuickModeChip("⚡", "مراجعة سريعة", AccentPractice,   PracticeGenerationType.QUICK_REVIEW, onStartPractice)
            QuickModeChip("📉", "نقاط الضعف",  ScoreLow,         PracticeGenerationType.WEAK_AREAS,   onStartPractice)
            QuickModeChip("🗂", "حسب الوحدة",  SubjectGeography, PracticeGenerationType.BY_UNIT,      onStartPractice)
            QuickModeChip("💡", "حسب المفهوم", SubjectChemistry, PracticeGenerationType.BY_CONCEPT,   onStartPractice)
            QuickModeChip("🔤", "حسب النوع",   SubjectArabic,    PracticeGenerationType.BY_TYPE,      onStartPractice)
        }
    }
}

@Composable
private fun QuickModeChip(
    emoji: String,
    label: String,
    color: Color,
    type: PracticeGenerationType,
    onClick: (PracticeGenerationType) -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick(type) }
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