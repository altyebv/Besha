package com.zeros.basheer.feature.lesson.presentation.components.cards


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.feature.lesson.presentation.components.foundation.*

/**
 * Overall progress summary card showing total lessons completion.
 *
 * Displays:
 * - Progress percentage in circular badge
 * - Animated progress bar
 * - Completed vs remaining lesson counts
 * - Total units and lessons count
 *
 * @param totalLessons Total number of lessons
 * @param completedLessons Number of completed lessons
 * @param totalUnits Total number of units
 * @param modifier Standard modifier
 * @param onStatsClick Optional callback when card is clicked for detailed stats
 */
@Composable
fun OverallProgressCard(
    totalLessons: Int,
    completedLessons: Int,
    totalUnits: Int,
    modifier: Modifier = Modifier,
    onStatsClick: (() -> Unit)? = null
) {
    val progress = calculateProgress(completedLessons, totalLessons)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = LessonAnimations.progressAnimation,
        label = "overall_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Overall progress: ${(progress * 100).toInt()}% complete"
            },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(LessonMetrics.internalSpacing)
        ) {
            // Header row with title and percentage badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and subtitle
                Column {
                    Text(
                        text = "تقدمك في هذه المادة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${formatUnitCount(totalUnits)} • ${formatLessonCount(totalLessons)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                // Percentage badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LessonMetrics.progressBarHeightLarge)
                    .clip(MaterialTheme.shapes.small),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "مكتمل",
                    value = "$completedLessons",
                    icon = Icons.Default.CheckCircle,
                    color = LessonColors.completed()
                )
                StatItem(
                    label = "متبقي",
                    value = "${totalLessons - completedLessons}",
                    icon = Icons.Default.PendingActions,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ============================================================================
// HELPER COMPONENTS
// ============================================================================

/**
 * Small stat item showing icon, value, and label.
 * Used in OverallProgressCard for completed/remaining counts.
 */
@Composable
fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(LessonMetrics.mediumIconSize)
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}