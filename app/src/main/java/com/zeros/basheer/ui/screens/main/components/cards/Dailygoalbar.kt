package com.zeros.basheer.ui.screens.main.components.cards

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.core.ui.theme.Success
import com.zeros.basheer.core.ui.theme.SuccessContainer
import com.zeros.basheer.feature.streak.domain.model.DailyActivity

/**
 * Daily goal progress bar — the Duolingo heartbeat.
 *
 * Shows progress toward the daily lesson goal using dot indicators.
 * Small, unobtrusive, but psychologically anchoring — always visible
 * below the header to remind students what's left for today.
 *
 * States:
 * - In progress: X of GOAL dots filled in amber
 * - Complete: all dots green + celebratory checkmark
 * - No activity yet: 0 dots filled, nudge label
 */
@Composable
fun DailyGoalBar(
    todayActivity: DailyActivity?,
    dailyGoal: Int = 3,
    modifier: Modifier = Modifier
) {
    val lessonsToday = todayActivity?.lessonsCompleted ?: 0
    val isComplete = lessonsToday >= dailyGoal
    val feedCardsToday = todayActivity?.feedCardsReviewed ?: 0

    val containerColor by animateColorAsState(
        targetValue = if (isComplete)
            SuccessContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(600),
        label = "goal_bg"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: label + sub-activity count
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = if (isComplete) "أحسنت! هدف اليوم مكتمل 🎉" else "هدف اليوم",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isComplete) Success
                    else MaterialTheme.colorScheme.onSurface
                )
                if (!isComplete && feedCardsToday > 0) {
                    Text(
                        text = "$feedCardsToday بطاقة مراجعة",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!isComplete && lessonsToday == 0) {
                    Text(
                        text = "ابدأ درسك الأول اليوم",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: dot indicators
            AnimatedContent(
                targetState = isComplete,
                transitionSpec = {
                    (fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.6f))
                        .togetherWith(fadeOut(tween(200)))
                },
                label = "goal_indicator"
            ) { complete ->
                if (complete) {
                    // All done — checkmark circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Success),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "مكتمل",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    // Dot progress
                    GoalDots(
                        completed = lessonsToday,
                        total = dailyGoal
                    )
                }
            }
        }
    }
}

// ── Dot indicators ─────────────────────────────────────────────────────────────

@Composable
private fun GoalDots(completed: Int, total: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Count label
        Text(
            text = "$completed/$total",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (completed > 0) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.width(4.dp))

        repeat(total) { index ->
            val isFilled = index < completed
            GoalDot(isFilled = isFilled, index = index)
        }
    }
}

@Composable
private fun GoalDot(isFilled: Boolean, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(isFilled) {
        if (isFilled) {
            kotlinx.coroutines.delay(index * 100L)
            visible = true
        } else {
            visible = false
        }
    }

    // Start visible if not filled (empty dots show immediately)
    val dotColor by animateColorAsState(
        targetValue = if (isFilled) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline,
        animationSpec = tween(300),
        label = "dot_color_$index"
    )

    val dotScale by animateFloatAsState(
        targetValue = if (isFilled) 1f else 0.75f,
        animationSpec = tween(300),
        label = "dot_scale_$index"
    )

    Box(
        modifier = Modifier
            .scale(dotScale)
            .size(12.dp)
            .clip(CircleShape)
            .background(dotColor)
    )
}