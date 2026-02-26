package com.zeros.basheer.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.streak.data.entity.StreakLevel

/**
 * A single day cell in the activity calendar grid.
 *
 * Visual states:
 *  - Padding cell  → fully transparent, no content
 *  - Future cell   → transparent background, no day number shown
 *  - COLD day      → muted [surfaceVariant] background, gray day number
 *  - SPARK day     → amber [StreakSpark] fill, white day number
 *  - FLAME day     → vivid orange [StreakFlame] fill, white day number + subtle glow ring
 *  - Today         → primary-colored border ring regardless of fill
 */
@Composable
fun ActivityDayCell(
    cell: CalendarCell,
    modifier: Modifier = Modifier
) {
    if (cell.isPadding) {
        // Invisible spacer — keeps the grid aligned
        Box(modifier = modifier.aspectRatio(1f))
        return
    }

    val fill = ProfileColors.cellFill(
        level = cell.level,
        isFuture = cell.isFuture,
        isEmpty = cell.isPadding
    )

    val shape = MaterialTheme.shapes.extraSmall

    // Border: today gets a 2dp primary ring; FLAME gets a subtle glow ring; others nothing
    val borderModifier = when {
        cell.isToday -> Modifier.border(
            width = 2.dp,
            color = ProfileColors.todayBorder(),
            shape = shape
        )
        cell.level == StreakLevel.FLAME && !cell.isFuture -> Modifier.border(
            width = 1.dp,
            color = ProfileColors.flameGlow(),
            shape = shape
        )
        else -> Modifier
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(fill)
            .then(borderModifier),
        contentAlignment = Alignment.Center
    ) {
        if (!cell.isFuture && cell.dayNumber != null) {
            val textColor = when {
                cell.level == StreakLevel.FLAME || cell.level == StreakLevel.SPARK ->
                    Color.White
                cell.isToday ->
                    MaterialTheme.colorScheme.primary
                else ->
                    MaterialTheme.colorScheme.onSurfaceVariant
            }
            Text(
                text = cell.dayNumber.toString(),
                fontSize = 10.sp,
                fontWeight = if (cell.isToday || cell.level != StreakLevel.COLD)
                    FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp
            )
        }
    }
}