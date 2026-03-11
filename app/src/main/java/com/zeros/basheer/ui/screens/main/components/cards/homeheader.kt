package com.zeros.basheer.ui.screens.main.components.cards

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.streak.data.entity.StreakLevel
import com.zeros.basheer.feature.user.domain.model.XpSummary
import com.zeros.basheer.ui.screens.main.components.foundation.*

@Composable
fun HomeHeader(
    userName: String,
    streakDays: Int,
    streakLevel: StreakLevel,
    overallProgress: Float,
    completedLessons: Int,
    totalLessons: Int,
    xpSummary: XpSummary? = null,
    // Passed from the parent once the list has scrolled — drives the shadow.
    hasScrolled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = overallProgress,
        animationSpec = MainAnimations.progressAnimationSpec,
        label = "header_progress"
    )
    val currentHour = remember {
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    }
    val greeting = remember(currentHour) { greetingByHour(currentHour) }

    // Shadow grows in as soon as any content scrolls under the header.
    val shadowElevation by animateDpAsState(
        targetValue = if (hasScrolled) 16.dp else 0.dp,
        animationSpec = tween(250),
        label = "header_shadow"
    )

    // Shape: square at the top (flushes against the screen edge / status bar),
    // rounded at the bottom for a floating-card feel.
    val headerShape = RoundedCornerShape(
        topStart = 20.dp, topEnd = 20.dp,
        bottomStart = 20.dp, bottomEnd = 20.dp
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp,0.dp)
            // Shadow drawn before clip so it renders outside the shape boundary.
            .shadow(elevation = shadowElevation, shape = headerShape, clip = false)
            .clip(headerShape)
            .background(
                Brush.linearGradient(colors = listOf(Color(0xFFF59E0B), Color(0xFFF97316)))
            )
    ) {
        // Decorative circle — unchanged
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.09f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Push content below the status bar so system icons don't
                // overlap the greeting text. The gradient background still
                // extends all the way to the top of the screen.
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ── Single row: greeting + chips ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Greeting + name on one line
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = userName.ifBlank { "بشير" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                // Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    xpSummary?.let { HeaderXpChip(it) }
                    HeaderStreakChip(days = streakDays, level = streakLevel)
                }
            }

            // ── Progress bar — only shown once study has started ─────────
            if (overallProgress > 0f) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
            }
        }
    }
}

// ── Streak chip ───────────────────────────────────────────────────────────────

@Composable
fun HeaderStreakChip(
    days: Int,
    level: StreakLevel,
    modifier: Modifier = Modifier
) {
    val shouldPulse = shouldPulseStreak(days, level)
    // Only create the infinite transition when it will actually animate.
    // When shouldPulse is false the transition object is never allocated and
    // the composition skips the animation entirely — saves a measurable amount
    // of work per frame when the streak is cold or zero.
    val scale = if (shouldPulse) {
        val infiniteTransition = rememberInfiniteTransition(label = "streak_pulse")
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue  = 1.06f,
            animationSpec = MainAnimations.streakPulseSpec,
            label = "streak_scale"
        ).value
    } else {
        1f
    }

    Surface(
        modifier = modifier.scale(scale),
        shape = MaterialTheme.shapes.small,
        color = Color.White.copy(alpha = 0.22f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (days > 0) Icons.Filled.LocalFireDepartment
                else Icons.Outlined.LocalFireDepartment,
                contentDescription = null,
                tint = if (level == StreakLevel.COLD) Color.White.copy(0.6f) else Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "$days",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ── XP chip ───────────────────────────────────────────────────────────────────

@Composable
private fun HeaderXpChip(xpSummary: XpSummary) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = Color.White.copy(alpha = 0.22f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${xpSummary.level}",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Text(
                text = "${xpSummary.totalXp} XP",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

private fun greetingByHour(hour: Int): String = when (hour) {
    in 5..11  -> "صباح الخير،"
    in 12..16 -> "مرحباً،"
    in 17..20 -> "مساء الخير،"
    else      -> "أهلاً بك،"
}