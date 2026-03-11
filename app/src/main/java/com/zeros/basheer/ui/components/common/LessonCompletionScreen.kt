package com.zeros.basheer.ui.components.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.core.ui.theme.Amber
import com.zeros.basheer.core.ui.theme.AmberDeep
import com.zeros.basheer.core.ui.theme.AmberLight
import com.zeros.basheer.core.ui.theme.Success

/**
 * Floating completion card — rendered over blurred/dimmed lesson content.
 * Not full-screen; caller positions it (typically bottom-anchored in a Box).
 *
 * MID-PART  — compact: check circle, "الجزء N مكتمل", stat row, CTA.
 * LAST PART — amber gradient header + trophy, stat row, optional next-lesson strip, CTA.
 */
@Composable
fun LessonCompletionScreen(
    lessonTitle: String,
    xpEarned: Int,
    readingTimeSeconds: Long,
    isRepeatCompletion: Boolean,
    isLastPart: Boolean,
    currentPartIndex: Int,
    totalParts: Int,
    checkpointScore: Pair<Int, Int>? = null,
    nextLessonTitle: String? = null,
    onContinue: () -> Unit,
    onBackToLessons: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLastPart) {
        LessonCompleteCard(
            lessonTitle        = lessonTitle,
            xpEarned           = xpEarned,
            readingTimeSeconds = readingTimeSeconds,
            isRepeatCompletion = isRepeatCompletion,
            checkpointScore    = checkpointScore,
            nextLessonTitle    = nextLessonTitle,
            onContinue         = onContinue,
            modifier           = modifier
        )
    } else {
        PartCompleteCard(
            currentPartIndex   = currentPartIndex,
            totalParts         = totalParts,
            xpEarned           = xpEarned,
            readingTimeSeconds = readingTimeSeconds,
            checkpointScore    = checkpointScore,
            onContinue         = onContinue,
            modifier           = modifier
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Mid-part card — compact, quiet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PartCompleteCard(
    currentPartIndex: Int,
    totalParts: Int,
    xpEarned: Int,
    readingTimeSeconds: Long,
    checkpointScore: Pair<Int, Int>?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remaining = totalParts - currentPartIndex - 1

    val scale by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "checkScale"
    )

    Surface(
        modifier        = modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(28.dp),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 24.dp,
        tonalElevation  = 4.dp
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Check circle
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Check,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onPrimary,
                        modifier           = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text       = "الجزء ${currentPartIndex + 1} مكتمل",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign  = TextAlign.Center,
                color      = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = when (remaining) {
                    1    -> "جزء واحد متبقٍ لإتمام الدرس"
                    else -> "$remaining أجزاء متبقية"
                },
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            CompletionStatRow(
                xpEarned           = xpEarned,
                readingTimeSeconds = readingTimeSeconds,
                checkpointScore    = checkpointScore
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick  = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text       = "تابع الجزء ${currentPartIndex + 2} →",
                    fontWeight = FontWeight.ExtraBold,
                    style      = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Final lesson card — earned, amber header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LessonCompleteCard(
    lessonTitle: String,
    xpEarned: Int,
    readingTimeSeconds: Long,
    isRepeatCompletion: Boolean,
    checkpointScore: Pair<Int, Int>?,
    nextLessonTitle: String?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier        = modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(28.dp),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 28.dp,
        tonalElevation  = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Amber gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Amber.copy(alpha = 0.20f),
                                Amber.copy(alpha = 0.04f)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
                    .padding(top = 24.dp, bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(AmberLight, Amber.copy(alpha = 0.18f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.EmojiEvents,
                            contentDescription = null,
                            tint               = Amber,
                            modifier           = Modifier.size(34.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text       = if (isRepeatCompletion) "مراجعة ممتازة!" else "أحسنت! 🎉",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color      = if (isRepeatCompletion)
                                MaterialTheme.colorScheme.onSurface
                            else AmberDeep,
                            textAlign  = TextAlign.Center
                        )
                        Text(
                            text      = lessonTitle,
                            style     = MaterialTheme.typography.bodySmall,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Card body
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isRepeatCompletion) {
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text      = "راجعت هذا الدرس من قبل — حصلت على XP مخفضة",
                            style     = MaterialTheme.typography.labelSmall,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                CompletionStatRow(
                    xpEarned           = xpEarned,
                    readingTimeSeconds = readingTimeSeconds,
                    checkpointScore    = checkpointScore
                )

                if (nextLessonTitle != null) {
                    NextLessonStrip(title = nextLessonTitle)
                }

                Button(
                    onClick  = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber,
                        contentColor   = AmberDeep
                    )
                ) {
                    Text(
                        text       = "العودة للدروس",
                        fontWeight = FontWeight.ExtraBold,
                        style      = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared sub-components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CompletionStatRow(
    xpEarned: Int,
    readingTimeSeconds: Long,
    checkpointScore: Pair<Int, Int>?,
    modifier: Modifier = Modifier
) {
    val minutes  = readingTimeSeconds / 60
    val seconds  = readingTimeSeconds % 60
    val timeText = when {
        minutes > 0 && seconds > 0 -> "${minutes}د ${seconds}ث"
        minutes > 0                -> "${minutes} دقيقة"
        else                       -> "${seconds} ثانية"
    }

    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompletionStatChip(
            value          = "+$xpEarned XP",
            label          = "نقاط",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier       = Modifier.weight(1f)
        )
        CompletionStatChip(
            value          = timeText,
            label          = "وقت القراءة",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor   = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier       = Modifier.weight(1f)
        )
        if (checkpointScore != null) {
            val (correct, total) = checkpointScore
            CompletionStatChip(
                value          = "$correct/$total",
                label          = "التحقق",
                containerColor = Success.copy(alpha = 0.12f),
                contentColor   = Success,
                modifier       = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NextLessonStrip(title: String) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector        = Icons.Outlined.AutoStories,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "الدرس التالي",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector        = Icons.Outlined.ArrowForward,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CompletionStatChip(
    value: String,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        color    = containerColor
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = contentColor
            )
            Text(
                text       = label,
                style      = MaterialTheme.typography.labelSmall,
                color      = contentColor.copy(alpha = 0.7f),
                textAlign  = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}