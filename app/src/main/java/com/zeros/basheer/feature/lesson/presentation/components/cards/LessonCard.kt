package com.zeros.basheer.feature.lesson.presentation.components.cards


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zeros.basheer.feature.lesson.domain.model.LessonDomain
import com.zeros.basheer.feature.lesson.presentation.components.foundation.LessonColors
import com.zeros.basheer.feature.lesson.presentation.components.foundation.LessonMetrics

/**
 * Card variant for lesson display
 */
enum class LessonCardVariant {
    /**
     * Full card with all details - used in main lessons list
     */
    FULL,

    /**
     * Compact card - used in search results or recommendations
     */
    COMPACT,

    /**
     * Minimal card - just title and icon
     */
    MINIMAL
}

/**
 * Individual lesson card component.
 *
 * Features:
 * - Completion status indicator
 * - Time estimate
 * - Completion badge
 * - Optional progress indicator
 * - Multiple variants for different contexts
 * - Proper accessibility support
 *
 * @param lesson Lesson domain model
 * @param isCompleted Whether lesson is completed
 * @param onClick Action when card is clicked
 * @param modifier Standard modifier
 * @param variant Display variant (FULL, COMPACT, MINIMAL)
 * @param showProgress Whether to show read progress bar
 * @param progressPercent Read progress (0.0 to 1.0)
 */
@Composable
fun LessonCard(
    lesson: LessonDomain,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: LessonCardVariant = LessonCardVariant.FULL,
    showProgress: Boolean = false,
    progressPercent: Float = 0f
) {
    when (variant) {
        LessonCardVariant.FULL -> LessonCardFull(
            lesson = lesson,
            isCompleted = isCompleted,
            onClick = onClick,
            showProgress = showProgress,
            progressPercent = progressPercent,
            modifier = modifier
        )
        LessonCardVariant.COMPACT -> LessonCardCompact(
            lesson = lesson,
            isCompleted = isCompleted,
            onClick = onClick,
            modifier = modifier
        )
        LessonCardVariant.MINIMAL -> LessonCardMinimal(
            lesson = lesson,
            isCompleted = isCompleted,
            onClick = onClick,
            modifier = modifier
        )
    }
}

// ============================================================================
// FULL VARIANT
// ============================================================================

@Composable
private fun LessonCardFull(
    lesson: LessonDomain,
    isCompleted: Boolean,
    onClick: () -> Unit,
    showProgress: Boolean,
    progressPercent: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent,
        label = "lesson_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClickLabel = "Open lesson: ${lesson.title}"
            ) { onClick() }
            .semantics {
                role = Role.Button
                contentDescription = buildString {
                    append("Lesson: ${lesson.title}")
                    if (isCompleted) append(", Completed")
                    append(", ${lesson.estimatedMinutes} minutes")
                }
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                LessonColors.completedBackground()
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = LessonColors.cardElevation(isCompleted)
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LessonMetrics.contentPadding),
                horizontalArrangement = Arrangement.spacedBy(LessonMetrics.internalSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator
                StatusIndicator(isCompleted = isCompleted)

                // Lesson info
                LessonInfo(
                    lesson = lesson,
                    isCompleted = isCompleted,
                    modifier = Modifier.weight(1f)
                )

                // Arrow icon
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            // Optional progress bar
            if (showProgress && progressPercent > 0f && !isCompleted) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LessonMetrics.progressBarHeight),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// COMPACT VARIANT
// ============================================================================

@Composable
private fun LessonCardCompact(
    lesson: LessonDomain,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isCompleted) {
            LessonColors.completedBackground()
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small status icon
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                contentDescription = null,
                tint = if (isCompleted) LessonColors.completed() else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            // Title
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Time
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "${lesson.estimatedMinutes}د",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// MINIMAL VARIANT
// ============================================================================

@Composable
private fun LessonCardMinimal(
    lesson: LessonDomain,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                contentDescription = null,
                tint = if (isCompleted) LessonColors.completed() else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============================================================================
// INTERNAL COMPONENTS
// ============================================================================

@Composable
private fun StatusIndicator(
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(LessonMetrics.statusIconSize)
            .clip(CircleShape)
            .background(
                if (isCompleted) {
                    LessonColors.completedIconBackground()
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
            contentDescription = if (isCompleted) "مكتمل" else "غير مكتمل",
            tint = if (isCompleted) {
                LessonColors.completed()
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(LessonMetrics.statusIconInner)
        )
    }
}

@Composable
private fun LessonInfo(
    lesson: LessonDomain,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LessonMetrics.tinySpacing)
    ) {
        // Title
        Text(
            text = lesson.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Metadata row
        Row(
            horizontalArrangement = Arrangement.spacedBy(LessonMetrics.internalSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time estimate
            Row(
                horizontalArrangement = Arrangement.spacedBy(LessonMetrics.tinySpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(LessonMetrics.miniIconSize)
                )
                Text(
                    text = "${lesson.estimatedMinutes} دقيقة",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Completion badge
            AnimatedVisibility(visible = isCompleted) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = LessonColors.completedBadge()
                ) {
                    Text(
                        text = "✓ مكتمل",
                        modifier = Modifier.padding(
                            horizontal = LessonMetrics.smallBadgePaddingHorizontal,
                            vertical = LessonMetrics.smallBadgePaddingVertical
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = LessonColors.completed()
                    )
                }
            }
        }
    }
}