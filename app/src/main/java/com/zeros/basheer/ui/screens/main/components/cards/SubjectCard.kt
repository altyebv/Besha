package com.zeros.basheer.ui.screens.main.components.cards


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zeros.basheer.core.ui.theme.StreakFlame
import com.zeros.basheer.ui.screens.main.SubjectWithProgress
import com.zeros.basheer.ui.screens.main.components.foundation.MainAnimations
import com.zeros.basheer.ui.screens.main.components.foundation.MainMetrics

/**
 * Subject card displaying subject info, progress, and quick actions.
 *
 * Features:
 * - Subject name and stats (units, lessons)
 * - Status badge (New/Active/Almost Done)
 * - Next lesson preview
 * - Animated progress bar
 * - Continue and Practice buttons
 *
 * @param subjectWithProgress Subject data with progress
 * @param onClick Action when card body clicked
 * @param onContinueClick Action when Continue button clicked
 * @param onPracticeClick Action when Practice button clicked
 * @param modifier Standard modifier
 */
@Composable
fun SubjectCard(
    subjectWithProgress: SubjectWithProgress,
    onClick: () -> Unit,
    onContinueClick: () -> Unit,
    onPracticeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = calculateProgress(subjectWithProgress)

    // Animate progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = MainAnimations.progressAnimationSpec,
        label = "subject_progress"
    )

    val (badge, badgeColor) = determineSubjectBadge(subjectWithProgress, progress)
    val progressColor = getProgressColor(progress)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClickLabel = "Open ${subjectWithProgress.subject.nameAr}"
            ) { onClick() }
            .semantics {
                role = Role.Button
                contentDescription = "${subjectWithProgress.subject.nameAr}, " +
                        "${(progress * 100).toInt()}% complete, " +
                        "${subjectWithProgress.completedLessons} of ${subjectWithProgress.totalLessons} lessons"
            },
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(
            defaultElevation = MainMetrics.subjectCardElevation
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MainMetrics.subjectCardPadding),
            verticalArrangement = Arrangement.spacedBy(MainMetrics.subjectCardSpacing)
        ) {
            // Header with name and badge
            SubjectHeader(
                name = subjectWithProgress.subject.nameAr,
                unitsCount = subjectWithProgress.units.size,
                lessonsCount = subjectWithProgress.totalLessons,
                badge = badge,
                badgeColor = badgeColor,
                isComplete = progress >= 1.0f
            )

            // Next lesson preview
            if (subjectWithProgress.nextLessonTitle != null) {
                NextLessonPreview(
                    lessonTitle = subjectWithProgress.nextLessonTitle,
                    progressColor = progressColor
                )
            }

            // Progress section
            ProgressSection(
                progress = animatedProgress,
                completed = subjectWithProgress.completedLessons,
                total = subjectWithProgress.totalLessons,
                progressColor = progressColor
            )

            // Quick action buttons
            ActionButtons(
                hasNextLesson = subjectWithProgress.nextLessonTitle != null,
                progressColor = progressColor,
                onContinue = onContinueClick,
                onPractice = onPracticeClick
            )
        }
    }
}

// ============================================================================
// SUB-COMPONENTS
// ============================================================================

/**
 * Subject header with name, stats, and optional badge
 */
@Composable
private fun SubjectHeader(
    name: String,
    unitsCount: Int,
    lessonsCount: Int,
    badge: String?,
    badgeColor: Color,
    isComplete: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$unitsCount ${if (unitsCount == 1) "وحدة" else "وحدات"} • " +
                        "$lessonsCount ${if (lessonsCount == 1) "درس" else "دروس"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Badge or completion icon
        if (badge != null) {
            SubjectBadge(
                text = badge,
                color = badgeColor
            )
        } else if (isComplete) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "مكتمل",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Status badge (New/Active/Almost Done)
 */
@Composable
private fun SubjectBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

/**
 * Next lesson preview box
 */
@Composable
private fun NextLessonPreview(
    lessonTitle: String,
    progressColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = null,
                tint = progressColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "التالي: $lessonTitle",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Progress section with bar and stats
 */
@Composable
private fun ProgressSection(
    progress: Float,
    completed: Int,
    total: Int,
    progressColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "التقدم",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$completed/$total",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(MaterialTheme.shapes.small),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Action buttons (Continue and Practice)
 */
@Composable
private fun ActionButtons(
    hasNextLesson: Boolean,
    progressColor: Color,
    onContinue: () -> Unit,
    onPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Continue button
        Button(
            onClick = onContinue,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = progressColor
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (hasNextLesson) "متابعة" else "ابدأ",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Practice button
        OutlinedButton(
            onClick = onPractice,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Quiz,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "تدريب",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

/**
 * Calculate subject progress percentage
 */
private fun calculateProgress(subjectWithProgress: SubjectWithProgress): Float {
    return if (subjectWithProgress.totalLessons > 0) {
        subjectWithProgress.completedLessons.toFloat() / subjectWithProgress.totalLessons
    } else 0f
}

/**
 * Determine subject badge and color
 * Returns a pair of (badge text, color) or (null, transparent) if no badge
 */
@Composable
private fun determineSubjectBadge(
    subjectWithProgress: SubjectWithProgress,
    progress: Float
): Pair<String?, Color> {
    return when {
        // Almost done
        progress >= 0.8f && progress < 1.0f ->
            "قارب على الانتهاء 🎯" to Color(0xFFFF9800)

        // Active (studied within last 24 hours)
        subjectWithProgress.lastStudied != null &&
                System.currentTimeMillis() - subjectWithProgress.lastStudied < 24 * 60 * 60 * 1000 ->
            "نشط 🔥" to StreakFlame

        // New (no lessons completed)
        subjectWithProgress.completedLessons == 0 ->
            "جديد ✨" to MaterialTheme.colorScheme.primary

        // No badge
        else -> null to Color.Transparent
    }
}

/**
 * Get progress bar color based on completion percentage
 */
@Composable
private fun getProgressColor(progress: Float): Color {
    return when {
        progress >= 0.8f -> Color(0xFF4CAF50) // Green - almost done
        progress >= 0.5f -> Color(0xFFFF9800) // Orange - halfway
        else -> MaterialTheme.colorScheme.primary // Blue - starting
    }
}