package com.zeros.basheer.ui.screens.main.components.cards


import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.feature.streak.data.entity.StreakLevel
import com.zeros.basheer.ui.screens.main.components.foundation.*

/**
 * Overall stats banner displaying welcome message, streak, and progress.
 *
 * Features:
 * - Personalized greeting
 * - Animated streak badge with pulsing effect
 * - Overall progress bar
 * - Completed lessons count
 *
 * @param userName User's display name
 * @param streakDays Current streak count
 * @param streakLevel Activity level (FLAME/SPARK/COLD)
 * @param overallProgress Overall completion (0.0 to 1.0)
 * @param completedLessons Number of completed lessons
 * @param totalLessons Total number of lessons
 * @param modifier Standard modifier
 */
@Composable
fun OverallStatsBanner(
    userName: String,
    streakDays: Int,
    streakLevel: StreakLevel,
    overallProgress: Float,
    completedLessons: Int,
    totalLessons: Int,
    modifier: Modifier = Modifier
) {
    // Animate progress
    val animatedProgress by animateFloatAsState(
        targetValue = overallProgress,
        animationSpec = MainAnimations.progressAnimationSpec,
        label = "progress_animation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Overall stats: ${(overallProgress * 100).toInt()}% complete, $streakDays day streak"
            },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MainMetrics.bannerElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MainMetrics.bannerPadding),
            verticalArrangement = Arrangement.spacedBy(MainMetrics.bannerSpacing)
        ) {
            // Greeting + Streak badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GreetingSection(
                    userName = userName,
                    modifier = Modifier.weight(1f)
                )

                StreakBadge(
                    days = streakDays,
                    level = streakLevel
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            // Progress section
            ProgressSection(
                progress = animatedProgress,
                completedLessons = completedLessons,
                totalLessons = totalLessons
            )
        }
    }
}

// ============================================================================
// SUB-COMPONENTS
// ============================================================================

/**
 * Greeting section with welcome message
 */
@Composable
private fun GreetingSection(
    userName: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "مرحباً، $userName",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "استمر في التقدم الرائع!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

/**
 * Animated streak badge showing current streak
 */
@Composable
fun StreakBadge(
    days: Int,
    level: StreakLevel,
    modifier: Modifier = Modifier,
    animated: Boolean = true
) {
    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "streak_pulse")
    val streakScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (shouldPulseStreak(days, level)) 1.1f else 1f,
        animationSpec = MainAnimations.streakPulseSpec,
        label = "streak_scale"
    )

    Surface(
        modifier = modifier.then(
            if (animated) Modifier.scale(streakScale) else Modifier
        ),
        shape = MaterialTheme.shapes.medium,
        color = MainColors.streakBackground(level),
        shadowElevation = streakElevation(days)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = MainMetrics.streakBadgePadding,
                vertical = MainMetrics.streakBadgePaddingVertical
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (days > 0) {
                    Icons.Filled.LocalFireDepartment
                } else {
                    Icons.Outlined.LocalFireDepartment
                },
                contentDescription = "Streak: $days days",
                tint = MainColors.streakColor(level),
                modifier = Modifier.size(MainMetrics.streakIconSize)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainColors.streakColor(level)
                )
                Text(
                    text = "يوم",
                    style = MaterialTheme.typography.labelSmall,
                    color = MainColors.streakColor(level).copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Progress section with bar and lesson count
 */
@Composable
private fun ProgressSection(
    progress: Float,
    completedLessons: Int,
    totalLessons: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Progress header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "التقدم الإجمالي",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(MaterialTheme.shapes.small),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )

        // Lesson count
        Text(
            text = "$completedLessons من $totalLessons درس مكتمل",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}