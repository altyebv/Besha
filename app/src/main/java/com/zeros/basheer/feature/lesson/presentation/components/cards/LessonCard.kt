package com.zeros.basheer.feature.lesson.presentation.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.core.ui.theme.Success
import com.zeros.basheer.feature.lesson.domain.model.LessonDomain

/**
 * Compact numbered lesson row — curriculum list style.
 *
 * Three visual states:
 * - DONE     green number circle + ✓ icon, title dimmed + strikethrough, green bg tint
 * - NEXT     amber number circle (primary color), title bold, no tint — draws the eye
 * - LOCKED   muted number, muted title — not interactive disabled, just visually quiet
 *
 * The "next" lesson is determined by the caller — it's the first incomplete lesson
 * in the unit, passed as isNext = true.
 *
 * Layout:
 *   [●]  [lesson title                  ]  [⏱ 12 د]
 *        [▓▓░░░░  partial progress bar  ]  (if progress > 0 and not complete)
 */
@Composable
fun LessonRow(
    lesson: LessonDomain,
    lessonNumber: Int,
    isCompleted: Boolean,
    isNext: Boolean,
    completedPartCount: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    val isDone = isCompleted
    val rowBg = when {
        isDone -> Success.copy(alpha = 0.05f)
        isNext -> MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
        else   -> Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(rowBg)
            .clickable(
                role = Role.Button,
                onClickLabel = lesson.title
            ) { onClick() }
            .semantics {
                role = Role.Button
                contentDescription = buildString {
                    append(lesson.title)
                    if (isDone) append("، مكتمل")
                    if (isNext) append("، التالي")
                    append("، ${lesson.estimatedMinutes} دقيقة")
                }
            }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Number / status circle ─────────────────────────────────────────
        LessonStatusCircle(
            number = lessonNumber,
            isDone = isDone,
            isNext = isNext
        )

        // ── Title + optional partial progress / part indicator ────────────
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isDone -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    isNext -> MaterialTheme.colorScheme.onSurface
                    else   -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                },
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Partial read progress indicator — shown when started but not done
            if (!isDone && lesson.progress > 0f) {
                if (lesson.partCount > 1 && completedPartCount > 0) {
                    // Multi-part: show which part to resume instead of a generic bar
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text       = "استمر من الجزء ${completedPartCount + 1}",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // Single-part or not yet started any part: simple progress bar
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress    = { lesson.progress },
                            modifier    = Modifier
                                .width(64.dp)
                                .height(2.dp)
                                .clip(MaterialTheme.shapes.small),
                            color       = MaterialTheme.colorScheme.primary,
                            trackColor  = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text       = "تابع",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Part pills — shown when lesson has more than one part
            if (lesson.partCount > 1) {
                LessonPartProgress(
                    completedParts = completedPartCount,
                    totalParts = lesson.partCount,
                    isLessonComplete = isCompleted
                )
            }
        }

        // ── Time estimate ──────────────────────────────────────────────────
        Text(
            text = "${lesson.estimatedMinutes} د",
            style = MaterialTheme.typography.labelSmall,
            color = if (isNext) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ── Number/status circle ───────────────────────────────────────────────────────

@Composable
private fun LessonStatusCircle(
    number: Int,
    isDone: Boolean,
    isNext: Boolean
) {
    val bgColor = when {
        isDone -> Success.copy(alpha = 0.15f)
        isNext -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else   -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isDone -> Success
        isNext -> MaterialTheme.colorScheme.primary
        else   -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (isDone) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
        } else {
            Text(
                text = "%02d".format(number),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )
        }
    }
}

// ── Part pills ─────────────────────────────────────────────────────────────────

/**
 * Segmented part-progress track shown below the lesson title when [totalParts] > 1.
 *
 * Visual states:
 * - All done (isLessonComplete) → all segments amber, "مكتمل ✓" label
 * - In progress                 → filled segments amber, empty segments faded
 * - Not started                 → all segments faded amber, "X أجزاء" label
 *
 * Segments are connected (no gap at start/end) to read as a single track,
 * with 3dp gaps between each segment.
 */
@Composable
private fun LessonPartProgress(
    completedParts: Int,
    totalParts: Int,
    isLessonComplete: Boolean,
    modifier: Modifier = Modifier
) {
    val Amber = Color(0xFFF59E0B)
    val inProgress = completedParts > 0 && !isLessonComplete

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Segment track
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(totalParts) { index ->
                val filled = isLessonComplete || index < completedParts
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(if (filled) Amber else Amber.copy(alpha = 0.18f))
                )
            }
        }

        // Label
        Text(
            text = when {
                isLessonComplete -> "$totalParts / $totalParts ✓"
                inProgress       -> "$completedParts / $totalParts"
                else             -> "$totalParts أجزاء"
            },
            style      = MaterialTheme.typography.labelSmall,
            fontSize   = 10.sp,
            color      = when {
                isLessonComplete -> Success.copy(alpha = 0.75f)
                inProgress       -> Amber
                else             -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            },
            fontWeight = FontWeight.SemiBold
        )
    }
}