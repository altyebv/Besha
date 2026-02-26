package com.zeros.basheer.feature.user.presentation.settings.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// CONSTANTS
// ─────────────────────────────────────────────────────────────────────────────

/** Height of each item cell — shared with ReminderTimeDialog for the highlight band. */
internal val WHEEL_ITEM_HEIGHT = 48.dp

private const val VISIBLE_ITEMS = 5   // must be odd so the center slot is unambiguous

// ─────────────────────────────────────────────────────────────────────────────
// DRUM ROLL WHEEL
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A snap-scrolling drum-roll picker wheel.
 *
 * ### Design principles
 * - **Uncontrolled after mount** — [initialIndex] seeds the list position once.
 *   The parent never drives scroll position after that, eliminating the
 *   "controlled vs uncontrolled state fight" that caused AM/PM glitches.
 * - **Settled-only callbacks** — [onSettled] fires only when
 *   [LazyListState.isScrollInProgress] flips to `false`, avoiding continuous
 *   state churn during drag flings.
 * - **layoutInfo-based center detection** — finds the visible item whose
 *   geometric center is closest to the viewport center, rather than using
 *   arithmetic that breaks near list ends.
 * - **Scale animation, not font-size jump** — all items stay at the same
 *   font size; `Modifier.scale` + `Modifier.alpha` animate smoothly with a
 *   120 ms tween so there are no layout shifts inside fixed-height cells.
 *
 * @param items        The ordered list of values to display.
 * @param initialIndex Index in [items] to center on first composition.
 * @param label        Converts an item to its display string.
 * @param onSettled    Called with the centered item each time scrolling stops.
 * @param itemWidth    Optional fixed width; defaults to [Dp.Unspecified] (fills parent weight).
 */
@Composable
internal fun <T> DrumRollWheel(
    items:        List<T>,
    initialIndex: Int,
    label:        (T) -> String,
    onSettled:    (T) -> Unit,
    modifier:     Modifier = Modifier,
    itemWidth:    Dp = Dp.Unspecified
) {
    val halfVisible   = VISIBLE_ITEMS / 2
    val listState     = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialIndex - halfVisible).coerceAtLeast(0)
    )
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val scope         = rememberCoroutineScope()

    // ── Center detection via layoutInfo ───────────────────────────────────────
    val centeredIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            if (info.visibleItemsInfo.isEmpty()) return@derivedStateOf initialIndex

            val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo
                .minByOrNull { item ->
                    kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
                }
                ?.index ?: initialIndex
        }
    }

    // ── Fire callback only when scrolling fully stops ─────────────────────────
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val idx = centeredIndex.coerceIn(items.indices)
            onSettled(items[idx])
        }
    }

    // ── Layout ────────────────────────────────────────────────────────────────
    Box(
        modifier = modifier
            .height(WHEEL_ITEM_HEIGHT * VISIBLE_ITEMS)
            .then(
                if (itemWidth != Dp.Unspecified) Modifier.width(itemWidth) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        val fadeHeight = WHEEL_ITEM_HEIGHT * halfVisible
        val surface    = MaterialTheme.colorScheme.surface

        // Top fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fadeHeight)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(surface, Color.Transparent)))
        )
        // Bottom fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fadeHeight)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, surface)))
        )

        // Scrollable items
        LazyColumn(
            state               = listState,
            flingBehavior       = flingBehavior,
            contentPadding      = PaddingValues(vertical = WHEEL_ITEM_HEIGHT * halfVisible),
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(items) { index, item ->
                val isCentered = index == centeredIndex.coerceIn(items.indices)

                val scale by animateFloatAsState(
                    targetValue   = if (isCentered) 1.18f else 0.82f,
                    animationSpec = tween(durationMillis = 120),
                    label         = "wheelItemScale"
                )
                val alpha by animateFloatAsState(
                    targetValue   = if (isCentered) 1f else 0.28f,
                    animationSpec = tween(durationMillis = 120),
                    label         = "wheelItemAlpha"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WHEEL_ITEM_HEIGHT)
                        .clickable {
                            scope.launch {
                                listState.animateScrollToItem(
                                    (index - halfVisible).coerceAtLeast(0)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = label(item),
                        fontSize   = 20.sp,
                        fontWeight = if (isCentered) FontWeight.Bold else FontWeight.Normal,
                        color      = if (isCentered) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        textAlign  = TextAlign.Center,
                        modifier   = Modifier
                            .scale(scale)
                            .alpha(alpha)
                    )
                }
            }
        }
    }
}