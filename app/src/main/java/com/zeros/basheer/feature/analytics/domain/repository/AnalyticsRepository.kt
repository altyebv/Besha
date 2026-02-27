package com.zeros.basheer.feature.analytics.domain.repository

import com.zeros.basheer.feature.analytics.domain.model.BasheerEvent

interface AnalyticsRepository {

    /**
     * Enqueue a single event to the local Room queue.
     * This is fire-and-forget — the caller does not wait for Firestore.
     */
    suspend fun enqueue(event: BasheerEvent)

    /**
     * Upload all unsynced events to Firestore, grouped into one document per day.
     * Called exclusively by [AnalyticsSyncWorker] — not by UI code.
     * Returns the number of events successfully uploaded.
     */
    suspend fun uploadPendingBatches(): Result<Int>

    /**
     * Delete synced events older than [daysToKeep] days.
     * Keeps the local table from growing unbounded.
     */
    suspend fun pruneOldSyncedEvents(daysToKeep: Int = 30)
}