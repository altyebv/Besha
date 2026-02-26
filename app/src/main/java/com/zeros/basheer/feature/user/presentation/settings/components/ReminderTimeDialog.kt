package com.zeros.basheer.feature.user.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// DATA — wheel item lists (private, owned here)
// ─────────────────────────────────────────────────────────────────────────────

private val HOURS   = (1..12).toList()
private val MINUTES = (0..55 step 5).toList()
private val PERIODS = listOf(true, false)   // true = AM / ص

private fun h12ToH24(h12: Int, isAm: Boolean): Int = when {
    isAm  && h12 == 12 -> 0
    isAm               -> h12
    !isAm && h12 == 12 -> 12
    else               -> h12 + 12
}

// ─────────────────────────────────────────────────────────────────────────────
// DIALOG
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Drum-roll time picker wrapped in an [AlertDialog].
 *
 * Accepts and emits time in 24-hour format so the caller never needs to
 * think about AM/PM conversion.
 *
 * @param currentHour   Current hour in 24-hour format (0–23).
 * @param currentMinute Current minute (0–59; rounded to nearest 5 when displayed).
 * @param onConfirm     Called with the selected hour (0–23) and minute.
 */
@Composable
internal fun ReminderTimeDialog(
    currentHour: Int,
    currentMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    // ── Single source of truth ────────────────────────────────────────────────
    var selectedHour24 by remember { mutableIntStateOf(currentHour) }
    var selectedMinute by remember { mutableIntStateOf(currentMinute) }

    // Pure derivations — never separate mutable state
    val isAm = selectedHour24 < 12
    val displayH12 = when {
        selectedHour24 == 0  -> 12
        selectedHour24 == 12 -> 12
        selectedHour24 > 12  -> selectedHour24 - 12
        else                 -> selectedHour24
    }
    val minuteSnapped = MINUTES.minByOrNull { kotlin.math.abs(it - selectedMinute) } ?: 0

    // ── Period key — forces hours wheel to remount when AM/PM flips ───────────
    // Both "8 AM" and "8 PM" map to displayH12 = 8, so the hours wheel wouldn't
    // detect any index change on its own. Bumping this key discards the old
    // LazyListState and creates a fresh one seeded at the correct position.
    var periodKey by remember { mutableIntStateOf(if (isAm) 0 else 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text       = "وقت التذكير",
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Live preview
                Text(
                    text       = formatTime(selectedHour24, selectedMinute),
                    style      = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )

                // Wheels container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    // Shared center-row highlight band — one box behind all three wheels
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(WHEEL_ITEM_HEIGHT)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    )

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        // Hours (1–12)
                        // key = periodKey forces remount when period flips
                        key(periodKey) {
                            DrumRollWheel(
                                items        = HOURS,
                                initialIndex = HOURS.indexOf(displayH12).coerceAtLeast(0),
                                label        = { "$it" },
                                onSettled    = { h12 ->
                                    selectedHour24 = h12ToH24(h12, isAm)
                                },
                                modifier     = Modifier.weight(2.2f)
                            )
                        }

                        Text(
                            text       = ":",
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier   = Modifier.padding(bottom = 4.dp)
                        )

                        // Minutes (00, 05 … 55)
                        DrumRollWheel(
                            items        = MINUTES,
                            initialIndex = MINUTES.indexOf(minuteSnapped).coerceAtLeast(0),
                            label        = { it.toString().padStart(2, '0') },
                            onSettled    = { selectedMinute = it },
                            modifier     = Modifier.weight(2.2f)
                        )

                        // Vertical divider before ص/م
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(WHEEL_ITEM_HEIGHT * 3)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )

                        // AM / PM  (ص / م)
                        DrumRollWheel(
                            items        = PERIODS,
                            initialIndex = if (isAm) 0 else 1,
                            label        = { if (it) "ص" else "م" },
                            onSettled    = { nowAm ->
                                if (nowAm != isAm) {
                                    selectedHour24 = if (nowAm) {
                                        if (selectedHour24 >= 12) selectedHour24 - 12
                                        else selectedHour24
                                    } else {
                                        if (selectedHour24 < 12) selectedHour24 + 12
                                        else selectedHour24
                                    }
                                    // Remount hours wheel with fresh state
                                    periodKey++
                                }
                            },
                            itemWidth    = 52.dp,
                            modifier     = Modifier.weight(1.6f)
                        )
                    }
                }

                Text(
                    text      = "سيصلك التذكير يومياً في هذا الوقت",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedHour24, selectedMinute) }) {
                Text("حفظ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}