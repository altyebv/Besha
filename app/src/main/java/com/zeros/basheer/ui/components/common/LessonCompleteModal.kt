package com.zeros.basheer.ui.components.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Bottom sheet modal shown after a lesson is marked complete.
 *
 * Shows:
 * - Celebration header (first time vs repeat)
 * - XP earned + reading time + checkpoint score (when available)
 * - Forward pull strip pointing to the next lesson (when available)
 * - Back to lessons CTA
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonCompletionModal(
    lessonTitle: String,
    xpEarned: Int,
    readingTimeSeconds: Long,
    isRepeatCompletion: Boolean,
    onDismiss: () -> Unit,
    onBackToLessons: () -> Unit,
    // Phase 4 additions — optional, modal degrades gracefully if null
    checkpointScore: Pair<Int, Int>? = null,   // correct / total
    nextLessonTitle: String? = null,
    // Part context — drives headline copy and CTA label
    isLastPart: Boolean = true,
    currentPartIndex: Int = 0,
    totalParts: Int = 1,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Icon ──────────────────────────────────────────────────────────
            Icon(
                imageVector = when {
                    isRepeatCompletion -> Icons.Default.Replay
                    isLastPart         -> Icons.Default.CheckCircle
                    else               -> Icons.Outlined.ArrowForward
                },
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = when {
                    isRepeatCompletion -> MaterialTheme.colorScheme.tertiary
                    isLastPart         -> MaterialTheme.colorScheme.primary
                    else               -> Color(0xFFF59E0B)   // amber for mid-part
                }
            )

            // ── Headline ──────────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = when {
                        isRepeatCompletion -> "مراجعة ممتازة!"
                        isLastPart         -> "أحسنت! 🎉"
                        else               -> "الجزء ${currentPartIndex + 1} مكتمل ✓"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = when {
                        !isLastPart && totalParts > 1 ->
                            "جزء واحد أقل — ${totalParts - currentPartIndex - 1} جزء متبقي"
                        else -> lessonTitle
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // ── Stats row ─────────────────────────────────────────────────────
            val minutes = readingTimeSeconds / 60
            val seconds = readingTimeSeconds % 60
            val timeText = when {
                minutes > 0 && seconds > 0 -> "${minutes}د ${seconds}ث"
                minutes > 0 -> "${minutes} دقيقة"
                else -> "${seconds} ثانية"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(
                    label = "نقاط مكتسبة",
                    value = "+$xpEarned XP",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "وقت القراءة",
                    value = timeText,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                // Checkpoint score chip — only rendered when checkpoints were answered
                if (checkpointScore != null) {
                    val (correct, total) = checkpointScore
                    StatChip(
                        label = "نقاط التحقق",
                        value = "$correct/$total",
                        containerColor = Color(0xFFD1FAE5),
                        contentColor = Color(0xFF065F46),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Repeat note ───────────────────────────────────────────────────
            if (isRepeatCompletion) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "راجعت هذا الدرس من قبل — حصلت على XP مخفضة",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // ── Forward pull strip ────────────────────────────────────────────
            if (nextLessonTitle != null) {
                ForwardPullStrip(nextLessonTitle = nextLessonTitle)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── CTA ───────────────────────────────────────────────────────────
            Button(
                onClick = {
                    onDismiss()
                    onBackToLessons()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = if (isLastPart) "العودة للدروس"
                    else "تابع الجزء ${currentPartIndex + 2}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Forward pull strip ────────────────────────────────────────────────────────

@Composable
private fun ForwardPullStrip(nextLessonTitle: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoStories,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "الدرس التالي",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = nextLessonTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Stat chip ─────────────────────────────────────────────────────────────────

@Composable
private fun StatChip(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}