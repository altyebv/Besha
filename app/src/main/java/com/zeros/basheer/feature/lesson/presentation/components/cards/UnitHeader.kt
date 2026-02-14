package com.zeros.basheer.feature.lesson.presentation.components.cards


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.zeros.basheer.feature.lesson.presentation.components.foundation.LessonColors
import com.zeros.basheer.feature.lesson.presentation.components.foundation.LessonMetrics
import com.zeros.basheer.feature.lesson.presentation.components.foundation.calculateProgress
import com.zeros.basheer.feature.lesson.presentation.components.foundation.isFullyCompleted
import com.zeros.basheer.feature.subject.domain.model.Units

/**
 * Unit section header with progress tracking.
 *
 * Displays:
 * - Unit title and description
 * - Completion count badge
 * - Progress bar
 *
 * @param unit Unit domain model
 * @param completedLessons Number of completed lessons in this unit
 * @param totalLessons Total lessons in this unit
 * @param modifier Standard modifier
 * @param isCollapsible Future: support for collapsible units
 * @param onHeaderClick Future: action when header is clicked
 */
@Composable
fun UnitHeader(
    unit: Units,
    completedLessons: Int,
    totalLessons: Int,
    modifier: Modifier = Modifier,
    isCollapsible: Boolean = false,
    onHeaderClick: (() -> Unit)? = null
) {
    val progress = calculateProgress(completedLessons, totalLessons)
    val isComplete = isFullyCompleted(completedLessons, totalLessons)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = LessonMetrics.smallSpacing),
        verticalArrangement = Arrangement.spacedBy(LessonMetrics.smallSpacing)
    ) {
        // Title and progress badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title and description
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            ) {
                Text(
                    text = unit.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                unit.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = LessonMetrics.tinySpacing)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(LessonMetrics.internalSpacing))

            // Progress badge
            ProgressBadge(
                completedLessons = completedLessons,
                totalLessons = totalLessons,
                isComplete = isComplete
            )
        }

        // Progress bar (only show if not complete)
        if (!isComplete && totalLessons > 0) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LessonMetrics.progressBarHeight)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

// ============================================================================
// INTERNAL COMPONENTS
// ============================================================================

@Composable
private fun ProgressBadge(
    completedLessons: Int,
    totalLessons: Int,
    isComplete: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (isComplete) {
            LessonColors.completedIconBackground()
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = LessonMetrics.badgePaddingHorizontal,
                vertical = LessonMetrics.badgePaddingVertical
            ),
            horizontalArrangement = Arrangement.spacedBy(LessonMetrics.tinySpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isComplete) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = LessonColors.completed(),
                    modifier = Modifier.size(LessonMetrics.standardIconSize)
                )
            }
            Text(
                text = "$completedLessons/$totalLessons",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isComplete) {
                    LessonColors.completed()
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}