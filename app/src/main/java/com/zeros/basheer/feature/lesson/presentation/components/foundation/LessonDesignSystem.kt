package com.zeros.basheer.feature.lesson.presentation.components.foundation


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design system for Lessons feature.
 * Centralized colors, metrics, and animations.
 */

// ============================================================================
// COLOR SYSTEM
// ============================================================================

object LessonColors {
    /**
     * Color for completed lessons
     */
    fun completed() = Color(0xFF4CAF50)

    /**
     * Background tint for completed lesson cards
     */
    fun completedBackground() = completed().copy(alpha = 0.08f)

    /**
     * Background for completed lesson icons
     */
    fun completedIconBackground() = completed().copy(alpha = 0.15f)

    /**
     * Badge background for completed items
     */
    fun completedBadge() = completed().copy(alpha = 0.15f)

    /**
     * Color for in-progress/active lessons
     */
    @Composable
    fun inProgress() = MaterialTheme.colorScheme.primary

    /**
     * Container for in-progress items
     */
    @Composable
    fun inProgressContainer() = MaterialTheme.colorScheme.primaryContainer

    /**
     * Card elevation based on completion status
     */
    fun cardElevation(isCompleted: Boolean): Dp = if (isCompleted) 1.dp else 2.dp
}

// ============================================================================
// METRICS SYSTEM
// ============================================================================

object LessonMetrics {
    // Spacing
    val cardSpacing = 20.dp
    val sectionSpacing = 16.dp
    val contentPadding = 16.dp
    val internalSpacing = 12.dp
    val smallSpacing = 8.dp
    val tinySpacing = 4.dp

    // Card dimensions
    val cardCornerRadius = 16.dp
    val badgeCornerRadius = 12.dp
    val smallBadgeCornerRadius = 6.dp

    // Icon sizes
    val statusIconSize = 40.dp
    val statusIconInner = 24.dp
    val miniIconSize = 14.dp
    val standardIconSize = 16.dp
    val mediumIconSize = 18.dp

    // Progress indicators
    val progressBarHeight = 4.dp
    val progressBarHeightLarge = 10.dp
    val progressBarCornerRadius = 2.dp
    val progressBarCornerRadiusLarge = 5.dp

    // Badge sizing
    val badgePaddingHorizontal = 12.dp
    val badgePaddingVertical = 6.dp
    val smallBadgePaddingHorizontal = 6.dp
    val smallBadgePaddingVertical = 2.dp

    // TopBar
    val topBarElevation = 0.dp
}

// ============================================================================
// ANIMATION SYSTEM
// ============================================================================

object LessonAnimations {
    /**
     * Entry animation for cards sliding in from right
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
            initialOffsetX = { it / 3 }
        )
    }

    /**
     * Entry animation for section headers
     */
    val headerEntry: EnterTransition = fadeIn(
        animationSpec = tween(durationMillis = 200)
    ) + slideInVertically(
        animationSpec = tween(durationMillis = 200),
        initialOffsetY = { it / 4 }
    )

    /**
     * Entry animation for the progress card
     */
    val progressCardEntry: EnterTransition = fadeIn(
        animationSpec = tween(durationMillis = 300)
    ) + slideInVertically(
        animationSpec = tween(durationMillis = 300)
    )

    /**
     * Calculates staggered entry delay for list items
     */
    fun cardEntryDelay(index: Int): Int {
        return (300 + index * 80).coerceAtMost(800)
    }

    /**
     * Animation spec for progress bar
     */
    val progressAnimation: AnimationSpec<Float> = tween(
        durationMillis = 1000,
        easing = FastOutSlowInEasing
    )

    /**
     * Content transition for state changes
     */
    val contentTransition: ContentTransform = fadeIn() + slideInVertically() togetherWith fadeOut()

    /**
     * Initial delay for first item
     */
    const val INITIAL_DELAY = 100L

    /**
     * Delay for second wave of animations
     */
    const val SECOND_WAVE_DELAY = 200L
}

// ============================================================================
// SEMANTIC HELPERS
// ============================================================================

/**
 * Determines if a lesson set is fully completed
 */
fun isFullyCompleted(completed: Int, total: Int): Boolean {
    return total > 0 && completed >= total
}

/**
 * Calculates progress percentage
 */
fun calculateProgress(completed: Int, total: Int): Float {
    return if (total > 0) completed.toFloat() / total else 0f
}

/**
 * Formats lesson count in Arabic
 */
fun formatLessonCount(count: Int): String {
    return "$count ${if (count == 1) "درس" else "دروس"}"
}

/**
 * Formats unit count in Arabic
 */
fun formatUnitCount(count: Int): String {
    return "$count ${if (count == 1) "وحدة" else "وحدات"}"
}