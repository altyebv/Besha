package com.zeros.basheer.ui.screens.main.components.cards

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.core.ui.theme.*
import com.zeros.basheer.feature.streak.data.entity.StreakLevel
import com.zeros.basheer.feature.user.domain.model.XpSummary
import com.zeros.basheer.ui.screens.main.components.foundation.*

/**
 * Hero banner — the first thing users see each session.
 *
 * Design intent: A warm amber gradient card that communicates
 * momentum and progress immediately. The streak and XP badges
 * are first-class citizens, not afterthoughts.
 */
@Composable
fun OverallStatsBanner(
    userName: String,
    streakDays: Int,
    streakLevel: StreakLevel,
    overallProgress: Float,
    completedLessons: Int,
    totalLessons: Int,
    xpSummary: XpSummary? = null,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = overallProgress,
        animationSpec = MainAnimations.progressAnimationSpec,
        label = "overall_progress"
    )

    // Gradient: amber → warm orange — rich, warm, energetic
    val heroGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFF59E0B),  // Amber gold
            Color(0xFFF97316),  // Warm orange
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(heroGradient)
            .semantics {
                contentDescription = "التقدم الكلي: ${(overallProgress * 100).toInt()}%، سلسلة $streakDays يوم"
            }
    ) {
        // Decorative circle — top left
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = (-40).dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )
        // Decorative circle — bottom right
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MainMetrics.bannerPadding),
            verticalArrangement = Arrangement.spacedBy(MainMetrics.bannerSpacing)
        ) {
            // ── Top row: Greeting + Streak badge ──────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Greeting
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greetingByTime(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                // Streak + XP stacked
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StreakBadge(days = streakDays, level = streakLevel)
                    xpSummary?.let { XpLevelBadge(it) }
                }
            }

            // ── Progress section ──────────────────────────────────────────
            HeroBannerProgress(
                progress = animatedProgress,
                completedLessons = completedLessons,
                totalLessons = totalLessons
            )
        }
    }
}

// ============================================================================
// STREAK BADGE — pulsing fire icon with day count
// ============================================================================

@Composable
fun StreakBadge(
    days: Int,
    level: StreakLevel,
    modifier: Modifier = Modifier,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (shouldPulseStreak(days, level)) 1.08f else 1f,
        animationSpec = MainAnimations.streakPulseSpec,
        label = "streak_scale"
    )

    Surface(
        modifier = modifier.then(
            if (animated) Modifier.scale(scale) else Modifier
        ),
        shape = MaterialTheme.shapes.medium,
        color = Color.White.copy(alpha = 0.2f),
        shadowElevation = streakElevation(days)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (days > 0) Icons.Filled.LocalFireDepartment
                else Icons.Outlined.LocalFireDepartment,
                contentDescription = null,
                tint = if (level == StreakLevel.COLD) Color.White.copy(alpha = 0.6f)
                else Color.White,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "$days",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "يوم",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

// ============================================================================
// XP LEVEL BADGE
// ============================================================================

@Composable
private fun XpLevelBadge(xpSummary: XpSummary) {
    val animatedProgress by animateFloatAsState(
        targetValue = xpSummary.progressInLevel,
        animationSpec = androidx.compose.animation.core.tween(700),
        label = "xp_progress"
    )

    Surface(
        shape = MaterialTheme.shapes.small,
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level circle
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${xpSummary.level}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 9.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${xpSummary.totalXp} XP",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .width(64.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// ============================================================================
// PROGRESS — inside the hero banner
// ============================================================================

@Composable
private fun HeroBannerProgress(
    progress: Float,
    completedLessons: Int,
    totalLessons: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "التقدم الكلي",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        // Progress bar — white on amber
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(MainMetrics.progressBarHeight)
                .clip(MaterialTheme.shapes.small),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f)
        )

        Text(
            text = "$completedLessons من $totalLessons درس مكتمل",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

// ============================================================================
// HELPERS
// ============================================================================

/** Context-aware Arabic greeting by time of day */
private fun greetingByTime(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11  -> "صباح الخير،"
        in 12..16 -> "مرحباً،"
        in 17..20 -> "مساء الخير،"
        else      -> "أهلاً بك،"
    }
}