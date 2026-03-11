package com.zeros.basheer.feature.analytics.domain.model

/**
 * Three-tier analytics consent.
 *
 * FULL      — behavioral events + profile context (state, school, path, major).
 *             Name and email are NEVER included even in FULL mode.
 * ANONYMOUS — behavioral events only. No profile fields attached.
 *             The Firestore document contains only installId + events.
 * NONE      — no data leaves the device. [AnalyticsManager.track] is a no-op.
 */
enum class AnalyticsConsent {
    FULL,
    ANONYMOUS,
    NONE;

    /** True if any data should be collected and eventually uploaded. */
    val isEnabled: Boolean get() = this != NONE

    /** True if per-user profile context should be attached to uploads. */
    val includesProfile: Boolean get() = this == FULL

    companion object {
        /** Safe fallback when the stored string doesn't match any value. */
        fun fromString(value: String?): AnalyticsConsent =
            entries.firstOrNull { it.name == value } ?: ANONYMOUS
    }
}