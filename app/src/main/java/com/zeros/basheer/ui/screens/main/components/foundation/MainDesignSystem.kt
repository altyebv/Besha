package com.zeros.basheer.ui.screens.main.components.foundation


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zeros.basheer.core.ui.theme.StreakCold
import com.zeros.basheer.core.ui.theme.StreakFlame
import com.zeros.basheer.core.ui.theme.StreakSpark
import com.zeros.basheer.feature.streak.data.entity.StreakLevel

/**
 * Design system for Main screen.
 * Centralized colors, metrics, and animations for dashboard.
 */

// ============================================================================
// COLOR SYSTEM
// ============================================================================

object MainColors {
    /**
     * Streak colors based on activity level
     */
    fun streakColor(level: StreakLevel): Color = when (level) {
        StreakLevel.FLAME -> StreakFlame
        StreakLevel.SPARK -> StreakSpark
        StreakLevel.COLD -> StreakCold
    }

    /**
     * Streak background color with transparency
     */
    fun streakBackground(level: StreakLevel): Color = when (level) {
        StreakLevel.FLAME -> StreakFlame.copy(alpha = 0.2f)
        StreakLevel.SPARK -> StreakSpark.copy(alpha = 0.2f)
        StreakLevel.COLD -> StreakCold.copy(alpha = 0.15f)
    }

    /**
     * Subject card colors palette
     * Rotates through these for different subjects
     */
    val subjectColorsPalette = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFF9C27B0), // Purple
        Color(0xFFFF9800), // Orange
        Color(0xFFE91E63), // Pink
        Color(0xFF00BCD4), // Cyan
        Color(0xFF8BC34A), // Light Green
        Color(0xFFFF5722)  // Deep Orange
    )

    /**
     * Get subject color by index
     */
    fun subjectColor(index: Int): Color {
        return subjectColorsPalette[index % subjectColorsPalette.size]
    }

    /**
     * Progress ring track color
     */
    @Composable
    fun progressTrack() = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
}

// ============================================================================
// METRICS SYSTEM
// ============================================================================

object MainMetrics {
    // Screen spacing
    val contentPadding = 16.dp
    val verticalPadding = 20.dp
    val cardSpacing = 16.dp
    val sectionSpacing = 8.dp

    // Banner metrics
    val bannerPadding = 20.dp
    val bannerCornerRadius = 20.dp
    val bannerElevation = 4.dp
    val bannerSpacing = 16.dp

    // Streak badge
    val streakBadgeRadius = 16.dp
    val streakBadgePadding = 16.dp
    val streakBadgePaddingVertical = 10.dp
    val streakIconSize = 24.dp
    val streakBadgeElevation = 4.dp

    // Progress ring
    val progressRingSize = 64.dp
    val progressRingStroke = 6.dp
    val progressRingSmall = 48.dp
    val progressRingStrokeSmall = 4.dp

    // Subject card
    val subjectCardPadding = 16.dp
    val subjectCardCornerRadius = 16.dp
    val subjectCardElevation = 2.dp
    val subjectIconSize = 40.dp
    val subjectCardSpacing = 12.dp

    // Focus card
    val focusCardPadding = 20.dp
    val focusCardCornerRadius = 16.dp
    val focusCardElevation = 3.dp

    // Button metrics
    val buttonSpacing = 8.dp
    val buttonPadding = 12.dp
}

// ============================================================================
// ANIMATION SYSTEM
// ============================================================================

object MainAnimations {
    /**
     * Streak pulse animation
     * Scales between 1.0 and 1.1 continuously
     */
    val streakPulseSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(  // ← CHANGED TYPE
        animation = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        repeatMode = RepeatMode.Reverse
    )

    /**
     * Progress bar animation
     */
    val progressAnimationSpec: AnimationSpec<Float> = tween(
        durationMillis = 1000,
        easing = FastOutSlowInEasing
    )

    /**
     * Card entry animation with scale
     */
    fun cardEntry(index: Int): EnterTransition {
        return fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = cardEntryDelay(index)
            )
        ) + slideInHorizontally(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = cardEntryDelay(index)
            ),
            initialOffsetX = { it / 4 }
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = cardEntryDelay(index)
            ),
            initialScale = 0.9f
        )
    }

    /**
     * Focus card entry animation
     */
    val focusCardEntry: EnterTransition = fadeIn(
        animationSpec = tween(durationMillis = 400)
    ) + slideInVertically(
        animationSpec = tween(durationMillis = 400),
        initialOffsetY = { it / 2 }
    )

    /**
     * Focus card exit animation
     */
    val focusCardExit: ExitTransition = fadeOut(
        animationSpec = tween(durationMillis = 300)
    ) + slideOutVertically(
        animationSpec = tween(durationMillis = 300),
        targetOffsetY = { -it / 2 }
    )

    /**
     * Banner transition
     */
    val bannerTransition: ContentTransform =
        fadeIn() + slideInVertically() togetherWith fadeOut()

    /**
     * Calculates staggered entry delay for cards
     */
    fun cardEntryDelay(index: Int): Int {
        return (index * 100).coerceAtMost(1000)
    }

    /**
     * Initial delays
     */
    const val BANNER_DELAY = 100L
    const val FOCUS_CARD_DELAY = 300L
    const val FIRST_SUBJECT_DELAY = 0L
}

// ============================================================================
// STREAK HELPERS
// ============================================================================

/**
 * Get streak emoji based on level
 */
fun StreakLevel.emoji(): String = when (this) {
    StreakLevel.FLAME -> "🔥"
    StreakLevel.SPARK -> "⚡"
    StreakLevel.COLD -> "❄️"
}

/**
 * Get streak label in Arabic
 */
fun StreakLevel.label(): String = when (this) {
    StreakLevel.FLAME -> "نشط جداً"
    StreakLevel.SPARK -> "نشط"
    StreakLevel.COLD -> "غير نشط"
}

/**
 * Determine if streak should pulse
 */
fun shouldPulseStreak(days: Int, level: StreakLevel): Boolean {
    return days > 0 && level != StreakLevel.COLD
}

/**
 * Calculate streak badge elevation
 */
fun streakElevation(days: Int): Dp {
    return if (days > 0) MainMetrics.streakBadgeElevation else 0.dp
}