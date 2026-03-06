package com.zeros.basheer.feature.quizbank.presentation.practice

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zeros.basheer.feature.practice.domain.model.PracticeGenerationType
import com.zeros.basheer.feature.quizbank.domain.model.QuestionCounts

@Composable
internal fun PracticeModeContent(
    questionCounts: QuestionCounts?,
    averageScore: Float?,
    weakAreaCount: Int,
    isWeakAreaLoading: Boolean,
    onStartPractice: (PracticeGenerationType) -> Unit,
    onStartWeakAreaSession: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SmartRecommendationCard(
            averageScore = averageScore,
            weakAreaCount = weakAreaCount,
            isLoading = isWeakAreaLoading,
            onStart = onStartWeakAreaSession
        )

        QuickModeStrip(onStartPractice = onStartPractice)

        CustomSessionEntry(
            onClick = { onStartPractice(PracticeGenerationType.CUSTOM) }
        )

        if (questionCounts != null) {
            BankStatsRow(counts = questionCounts)
        }
    }
}