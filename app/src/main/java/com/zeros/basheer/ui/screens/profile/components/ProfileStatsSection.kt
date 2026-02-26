package com.zeros.basheer.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileStatsSection(
    currentStreak: Int,
    longestStreak: Int,
    totalLessons: Int,
    totalCards: Int,
    totalQuestions: Int,
    totalMinutes: Long,
    modifier: Modifier = Modifier
) {
    val studyTimeLabel = if (totalMinutes >= 60) "${totalMinutes / 60}س" else "${totalMinutes}د"

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ProfileMetrics.tinySpacing)
    ) {
        ProfileSectionTitle(text = "إحصائياتك")
        Spacer(Modifier.height(ProfileMetrics.smallSpacing))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ProfileMetrics.tinySpacing)
        ) {
            StatTile(
                icon = Icons.Default.LocalFireDepartment,
                value = "$currentStreak",
                label = "سلسلة حالية",
                accentColor = Color(0xFFF97316),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Outlined.EmojiEvents,
                value = "$longestStreak",
                label = "أطول سلسلة",
                accentColor = Color(0xFFFFB347),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Outlined.AutoStories,
                value = "$totalLessons",
                label = "دروس",
                accentColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ProfileMetrics.tinySpacing)
        ) {
            StatTile(
                icon = Icons.Outlined.Quiz,
                value = "$totalCards",
                label = "بطاقات",
                accentColor = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Outlined.CheckCircle,
                value = "$totalQuestions",
                label = "أسئلة",
                accentColor = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Outlined.AccessTime,
                value = studyTimeLabel,
                label = "وقت الدراسة",
                accentColor = Color(0xFF0EA5E9),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STAT TILE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatTile(
    icon: ImageVector,
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ProfileMetrics.statTilePadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(ProfileMetrics.statIconSize)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(ProfileMetrics.statIconInner)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}