package com.zeros.basheer.ui.screens.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.streak.data.entity.StreakLevel
import com.zeros.basheer.feature.streak.domain.model.DailyActivity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * A 4-week activity grid with:
 *  - A fixed day-of-week header row: ح إ ث أ خ ج س  (Sunday → Saturday)
 *  - 4 full rows, each representing one calendar week
 *  - Cells aligned to the correct column for their weekday
 *  - Padding cells inserted before the first day to fill the partial first row
 *
 * The grid always shows exactly 4 complete weeks ending on today's week,
 * so the most recent activity is always visible in the last row.
 */
@Composable
fun ActivityCalendarGrid(
    recentActivity: List<DailyActivity>,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }

    // Build activity lookup: ISO date string → StreakLevel
    val activityMap = remember(recentActivity) {
        recentActivity.associate { it.date to it.streakLevel }
    }

    // Build the 4-week grid of CalendarCells
    val weeks = remember(today, activityMap) {
        buildCalendarWeeks(today = today, activityMap = activityMap, weekCount = 4)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ProfileMetrics.calendarRowGap)
    ) {
        // ── Day-of-week header ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ProfileMetrics.calendarCellGap)
        ) {
            ArabicDayHeaders.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier
                        .weight(1f)
                        .height(ProfileMetrics.dayHeaderHeight),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // ── Week rows ─────────────────────────────────────────────────────────
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ProfileMetrics.calendarCellGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.forEach { cell ->
                    ActivityDayCell(
                        cell = cell,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GRID BUILDER
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Builds [weekCount] full calendar weeks (each 7 cells wide, Sunday→Saturday)
 * ending with the week that contains [today].
 *
 * The first row may start with padding cells if the earliest date doesn't
 * fall on a Sunday.
 */
private fun buildCalendarWeeks(
    today: LocalDate,
    activityMap: Map<String, StreakLevel>,
    weekCount: Int = 4
): List<List<CalendarCell>> {
    // Find the Sunday that starts the first of our [weekCount] weeks.
    // "Last Sunday" relative to today (or today itself if today is Sunday).
    val todaySundayOffset = if (today.dayOfWeek == DayOfWeek.SUNDAY) 0L
    else today.dayOfWeek.value.toLong()   // MONDAY=1 … SATURDAY=6
    val lastSunday = today.minusDays(todaySundayOffset)
    val firstSunday = lastSunday.minusWeeks((weekCount - 1).toLong())

    val weeks = mutableListOf<List<CalendarCell>>()

    repeat(weekCount) { weekIndex ->
        val weekStart = firstSunday.plusWeeks(weekIndex.toLong())
        val row = (0 until 7).map { dayOffset ->
            val date = weekStart.plusDays(dayOffset.toLong())
            val dateIso = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val isFuture = date.isAfter(today)
            val level = activityMap[dateIso] ?: StreakLevel.COLD

            CalendarCell(
                dayNumber = date.dayOfMonth,
                dateIso = dateIso,
                level = level,
                isToday = date == today,
                isFuture = isFuture,
                isPadding = false   // every cell is a real date in this approach
            )
        }
        weeks.add(row)
    }

    return weeks
}