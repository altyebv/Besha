package com.zeros.basheer.ui.screens.main.components.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.sp
import com.zeros.basheer.core.ui.theme.StreakFlame
import com.zeros.basheer.core.ui.theme.Success
import com.zeros.basheer.ui.screens.main.SubjectWithProgress
import com.zeros.basheer.ui.screens.main.components.foundation.MainAnimations
import com.zeros.basheer.ui.screens.main.components.foundation.MainColors
import com.zeros.basheer.ui.screens.main.components.foundation.MainMetrics

/**
 * Compact subject row card.
 *
 * Layout:
 *   ▌ [emoji]  [name          ]  [progress bar]  [practice btn]
 *              [next lesson…  ]
 *
 * - Full row tap → lessons screen
 * - Icon button (trailing) → practice
 * - Color strip (leading 4dp) identifies the subject at a glance
 * - No action buttons inside — keeps the list fast to scan
 */
@Composable
fun SubjectCard(
    subjectWithProgress: SubjectWithProgress,
    onClick: () -> Unit,
    onContinueClick: () -> Unit,
    onPracticeClick: () -> Unit,
    subjectIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val progress = if (subjectWithProgress.totalLessons > 0)
        subjectWithProgress.completedLessons.toFloat() / subjectWithProgress.totalLessons
    else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = MainAnimations.progressAnimationSpec,
        label = "subject_progress"
    )

    // Memoised by subject id — the name-based pattern matching only re-runs
    // when the subject itself changes, not on every recomposition.
    val subjectColor = remember(subjectWithProgress.subject.id) {
        MainColors.subjectColorByName(subjectWithProgress.subject.nameAr, subjectIndex)
    }
    val subjectEmoji = remember(subjectWithProgress.subject.id) {
        MainColors.subjectEmoji(subjectWithProgress.subject.nameAr)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(
                role = Role.Button,
                onClickLabel = "فتح ${subjectWithProgress.subject.nameAr}"
            ) { onClick() }
            .semantics {
                role = Role.Button
                contentDescription = "${subjectWithProgress.subject.nameAr}، " +
                        "${(progress * 100).toInt()}% مكتمل"
            },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Color strip ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(
                        color = subjectColor,
                        shape = RoundedCornerShape(
                            topStart = 12.dp, bottomStart = 12.dp
                        )
                    )
            )

            // ── Emoji badge ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(subjectColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = subjectEmoji, fontSize = 18.sp)
            }

            // ── Name + next lesson + progress bar ─────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Top row: name + badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subjectWithProgress.subject.nameAr,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(6.dp))
                    StatusIndicator(
                        progress = progress,
                        subjectWithProgress = subjectWithProgress,
                        color = subjectColor
                    )
                }

                // Next lesson or completion label
                Text(
                    text = when {
                        progress >= 1f -> "مكتمل ✓"
                        subjectWithProgress.nextLessonTitle != null ->
                            subjectWithProgress.nextLessonTitle
                        else -> "${subjectWithProgress.totalLessons} دروس"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (progress >= 1f) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Progress bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = subjectColor,
                    trackColor = subjectColor.copy(alpha = 0.15f)
                )
            }

            // ── Practice icon button ───────────────────────────────────────
            IconButton(
                onClick = onPracticeClick,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(subjectColor.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "تدريب ${subjectWithProgress.subject.nameAr}",
                    tint = subjectColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── Status indicator — compact, trailing, non-intrusive ───────────────────────

@Composable
private fun StatusIndicator(
    progress: Float,
    subjectWithProgress: SubjectWithProgress,
    color: Color
) {
    when {
        progress >= 1f -> {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Success,
                modifier = Modifier.size(16.dp)
            )
        }
        progress >= 0.8f -> {
            StatusPill(text = "🎯 ${(progress * 100).toInt()}%", color = color)
        }
        subjectWithProgress.lastStudied != null &&
                System.currentTimeMillis() - subjectWithProgress.lastStudied < 86_400_000L -> {
            StatusPill(text = "🔥 نشط", color = StreakFlame)
        }
        subjectWithProgress.completedLessons == 0 -> {
            StatusPill(text = "جديد", color = color)
        }
        else -> {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}