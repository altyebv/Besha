package com.zeros.basheer.feature.analytics.domain.repository

/**
 * Handles the users/{installId} Firestore document.
 *
 * This is separate from [AnalyticsRepository] by design:
 *  - Analytics = append-only event stream (write-heavy, batched by day)
 *  - User doc  = single mutable document (upserted on each sync run)
 *
 * Both use the same installId as their Firestore key.
 */
interface UserSyncRepository {

    /**
     * Upsert the user document to Firestore.
     * - Creates the document on first run (sets firstSeenAt)
     * - Updates lastSeenAt + stats on every subsequent run
     * - Attaches profile fields only when consent is FULL
     * - Safe to call even when consent is NONE (returns early)
     */
    suspend fun syncUser(): Result<Unit>
}