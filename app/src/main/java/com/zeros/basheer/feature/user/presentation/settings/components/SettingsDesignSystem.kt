package com.zeros.basheer.feature.user.presentation.settings.components

// ─────────────────────────────────────────────────────────────────────────────
// SETTINGS DESIGN SYSTEM
// Shared tokens and pure helpers for the settings feature.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Formats a 24-hour time value into a localised Arabic 12-hour display string.
 * e.g. formatTime(20, 30) → "8:30 م"
 *      formatTime(0, 5)   → "12:05 ص"
 */
internal fun formatTime(hour: Int, minute: Int): String {
    val period = if (hour < 12) "ص" else "م"
    val h = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else      -> hour
    }
    return "$h:${minute.toString().padStart(2, '0')} $period"
}