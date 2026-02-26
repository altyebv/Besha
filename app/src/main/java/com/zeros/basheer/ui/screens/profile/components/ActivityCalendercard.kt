package com.zeros.basheer.ui.screens.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.core.ui.theme.StreakCold
import com.zeros.basheer.core.ui.theme.StreakFlame
import com.zeros.basheer.core.ui.theme.StreakSpark
import com.zeros.basheer.feature.streak.domain.model.DailyActivity

/**
 * Card that wraps the activity calendar grid with:
 *  - Header row: "سجل النشاط" label + current streak badge
 *  - [ActivityCalendarGrid]: the 4-week aligned grid
 *  - Legend row: FLAME / SPARK / COLD color explanations
 */
@Composable
fun ActivityCalendarCard(
    recentActivity: List<DailyActivity>,
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ProfileMetrics.cardPadding),
            verticalArrangement = Arrangement.spacedBy(ProfileMetrics.innerSpacing)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            CalendarHeader(currentStreak = currentStreak)

            // ── Grid ──────────────────────────────────────────────────────────
            ActivityCalendarGrid(recentActivity = recentActivity)

            // ── Legend ────────────────────────────────────────────────────────
            CalendarLegend()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HEADER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CalendarHeader(currentStreak: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ProfileMetrics.smallSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = StreakFlame,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "سجل النشاط",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (currentStreak > 0) {
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = StreakFlame.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "$currentStreak 🔥",
                    modifier = Modifier.padding(
                        horizontal = ProfileMetrics.chipHorizontal,
                        vertical = 4.dp
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = StreakFlame
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LEGEND
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(color = StreakFlame, label = "جلسة عميقة 🔥")
        LegendDot(color = StreakSpark, label = "تفاعل خفيف ✨")
        LegendDot(color = StreakCold.copy(alpha = 0.3f), label = "لا نشاط")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}