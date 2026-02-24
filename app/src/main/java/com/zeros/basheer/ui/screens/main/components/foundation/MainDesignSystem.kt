package com.zeros.basheer.ui.screens.main.components.foundation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zeros.basheer.core.ui.theme.*
import com.zeros.basheer.feature.streak.data.entity.StreakLevel

// ============================================================================
// COLOR SYSTEM
// ============================================================================

object MainColors {

    fun streakColor(level: StreakLevel): Color = when (level) {
        StreakLevel.FLAME -> StreakFlame
        StreakLevel.SPARK -> StreakSpark
        StreakLevel.COLD -> StreakCold
    }

    fun streakBackground(level: StreakLevel): Color = when (level) {
        StreakLevel.FLAME -> StreakFlame.copy(alpha = 0.18f)
        StreakLevel.SPARK -> StreakSpark.copy(alpha = 0.18f)
        StreakLevel.COLD -> StreakCold.copy(alpha = 0.12f)
    }

    /**
     * Subject accent colors — each subject gets a consistent identity
     */
    fun subjectColor(index: Int): Color = SubjectPalette[index % SubjectPalette.size]

    /**
     * Subject color by keyword match (for named subjects)
     */
    fun subjectColorByName(nameAr: String, index: Int): Color = when {
        nameAr.contains("فيزياء") -> SubjectPhysics
        nameAr.contains("كيمياء") -> SubjectChemistry
        nameAr.contains("جغرافيا") -> SubjectGeography
        nameAr.contains("عربي") || nameAr.contains("لغة عربية") -> SubjectArabic
        nameAr.contains("إسلامية") || nameAr.contains("دين") -> SubjectIslamic
        nameAr.contains("عسكرية") || nameAr.contains("تربية وطنية") -> SubjectMilitary
        nameAr.contains("رياضيات") -> SubjectMath
        nameAr.contains("أحياء") || nameAr.contains("بيولوجيا") -> SubjectBiology
        nameAr.contains("تاريخ") -> SubjectHistory
        nameAr.contains("إنجليزية") || nameAr.contains("إنجليزي") -> SubjectEnglish
        else -> subjectColor(index)
    }

    /**
     * Subject emoji icons — adds personality to subject cards
     */
    fun subjectEmoji(nameAr: String): String = when {
        nameAr.contains("فيزياء") -> "⚛️"
        nameAr.contains("كيمياء") -> "🧪"
        nameAr.contains("جغرافيا") -> "🌍"
        nameAr.contains("عربي") || nameAr.contains("لغة عربية") -> "📖"
        nameAr.contains("إسلامية") || nameAr.contains("دين") -> "🌙"
        nameAr.contains("عسكرية") || nameAr.contains("تربية وطنية") -> "🎖️"
        nameAr.contains("رياضيات") -> "📐"
        nameAr.contains("أحياء") || nameAr.contains("بيولوجيا") -> "🧬"
        nameAr.contains("تاريخ") -> "📜"
        nameAr.contains("إنجليزية") || nameAr.contains("إنجليزي") -> "🌐"
        else -> "📚"
    }

    @Composable
    fun progressTrack(): Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
}

// ============================================================================
// METRICS SYSTEM
// ============================================================================

object MainMetrics {
    // Screen spacing
    val contentPadding = 16.dp
    val verticalPadding = 16.dp
    val cardSpacing = 14.dp
    val sectionSpacing = 8.dp

    // Banner (hero card)
    val bannerPadding = 20.dp
    val bannerSpacing = 14.dp
    val bannerElevation = 0.dp              // Flat — color does the work

    // Streak badge
    val streakBadgePadding = 14.dp
    val streakBadgePaddingVertical = 8.dp
    val streakIconSize = 22.dp

    // Subject card
    val subjectCardPadding = 16.dp
    val subjectCardElevation = 0.dp         // Flat, border does the work
    val subjectCardSpacing = 10.dp
    val subjectIconSize = 44.dp
    val subjectIconBgSize = 52.dp

    // Focus card
    val focusCardPadding = 18.dp
    val focusCardElevation = 2.dp

    // Progress bar
    val progressBarHeight = 8.dp
    val progressBarHeightThin = 5.dp
}

// ============================================================================
// ANIMATION SYSTEM
// ============================================================================

object MainAnimations {

    val streakPulseSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
    )

    val progressAnimationSpec: AnimationSpec<Float> = tween(
        durationMillis = 900,
        easing = FastOutSlowInEasing
    )

    fun cardEntry(index: Int): EnterTransition = fadeIn(
        animationSpec = tween(durationMillis = 280, delayMillis = cardEntryDelay(index))
    ) + slideInVertically(
        animationSpec = tween(durationMillis = 280, delayMillis = cardEntryDelay(index)),
        initialOffsetY = { it / 6 }
    )

    val focusCardEntry: EnterTransition = fadeIn(tween(400)) +
            slideInVertically(tween(400)) { it / 4 }

    val focusCardExit: ExitTransition = fadeOut(tween(280)) +
            slideOutVertically(tween(280)) { -it / 4 }

    val bannerTransition: ContentTransform = fadeIn(tween(300)) togetherWith fadeOut(tween(200))

    fun cardEntryDelay(index: Int): Int = (index * 80).coerceAtMost(600)

    const val BANNER_DELAY = 100L
    const val FOCUS_CARD_DELAY = 250L
}

// ============================================================================
// STREAK HELPERS
// ============================================================================

fun StreakLevel.emoji(): String = when (this) {
    StreakLevel.FLAME -> "🔥"
    StreakLevel.SPARK -> "⚡"
    StreakLevel.COLD -> "❄️"
}

fun StreakLevel.label(): String = when (this) {
    StreakLevel.FLAME -> "نشط جداً"
    StreakLevel.SPARK -> "نشط"
    StreakLevel.COLD -> "غير نشط"
}

fun shouldPulseStreak(days: Int, level: StreakLevel): Boolean =
    days > 0 && level != StreakLevel.COLD

fun streakElevation(days: Int): Dp = if (days > 0) 3.dp else 0.dp