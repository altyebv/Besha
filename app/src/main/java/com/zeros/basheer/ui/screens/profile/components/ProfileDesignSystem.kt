package com.zeros.basheer.ui.screens.profile.components


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.core.ui.theme.StreakFlame
import com.zeros.basheer.core.ui.theme.StreakSpark
import com.zeros.basheer.feature.streak.data.entity.StreakLevel

// ============================================================================
// SPACING TOKENS
// ============================================================================

object ProfileMetrics {
    val screenPadding     = 16.dp
    val cardPadding       = 18.dp
    val cardSpacing       = 12.dp
    val sectionSpacing    = 20.dp
    val innerSpacing      = 14.dp
    val smallSpacing      = 8.dp
    val tinySpacing       = 6.dp

    // Hero
    val heroVerticalPadding = 20.dp
    val avatarSize        = 68.dp
    val chipHorizontal    = 10.dp
    val chipVertical      = 7.dp

    // Stats
    val statTilePadding   = 12.dp
    val statIconSize      = 32.dp
    val statIconInner     = 17.dp

    // Calendar
    val calendarCellGap   = 5.dp
    val calendarRowGap    = 5.dp
    val dayHeaderHeight   = 16.dp
}

// ============================================================================
// COLOR HELPERS
// ============================================================================

object ProfileColors {

    /** Fill color for a calendar cell based on streak level and context. */
    @Composable
    fun cellFill(level: StreakLevel, isFuture: Boolean, isEmpty: Boolean): Color = when {
        isFuture || isEmpty -> Color.Transparent
        level == StreakLevel.FLAME -> StreakFlame
        level == StreakLevel.SPARK -> StreakSpark
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    /** Glow / alpha overlay color for FLAME cells. */
    fun flameGlow(): Color = StreakFlame.copy(alpha = 0.25f)

    /** Border color for today's cell. */
    @Composable
    fun todayBorder(): Color = MaterialTheme.colorScheme.primary

    /** Day number text color based on fill. */
    fun dayNumberColor(level: StreakLevel, isFuture: Boolean, isToday: Boolean): Color = when {
        isFuture -> Color.Transparent
        level == StreakLevel.FLAME || level == StreakLevel.SPARK -> Color.White
        isToday -> Color.Unspecified   // will use primary via caller
        else -> Color.Unspecified      // will use onSurfaceVariant via caller
    }
}

// ============================================================================
// CALENDAR HELPERS
// ============================================================================

/**
 * Arabic abbreviations for days of the week.
 * Index 0 = Sunday (ح), 1 = Monday (إ), ..., 6 = Saturday (س).
 * Matches the standard Arabic-language calendar layout (Sunday-first).
 */
val ArabicDayHeaders = listOf("ح", "إ", "ث", "أ", "خ", "ج", "س")

/**
 * Maps a [java.time.DayOfWeek] value to a 0-based column index
 * in a Sunday-first calendar grid.
 *
 * java.time.DayOfWeek: MONDAY=1 … SATURDAY=6, SUNDAY=7
 * Column:              Sunday=0, Monday=1, … Saturday=6
 */
fun dayOfWeekToColumnIndex(dayOfWeekValue: Int): Int =
    if (dayOfWeekValue == 7) 0 else dayOfWeekValue  // SUNDAY(7) → 0, others pass through

/**
 * Describes a single cell in the activity calendar grid.
 *
 * @param dayNumber  The calendar day of the month (1–31), or null for padding cells.
 * @param dateIso    ISO-8601 date string "YYYY-MM-DD", or null for padding cells.
 * @param level      Streak activity level for this day.
 * @param isToday    Whether this cell represents today.
 * @param isFuture   Whether this cell is in the future (should be blank).
 * @param isPadding  Whether this is an empty padding cell before the first day.
 */
data class CalendarCell(
    val dayNumber: Int?,
    val dateIso: String?,
    val level: StreakLevel,
    val isToday: Boolean,
    val isFuture: Boolean,
    val isPadding: Boolean
)

// ============================================================================
// SHARED UI ATOMS
// ============================================================================

/**
 * Standard section title used across all profile sub-sections.
 */
@Composable
fun ProfileSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}