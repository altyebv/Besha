package com.zeros.basheer.feature.analytics.domain.repository

import com.zeros.basheer.feature.analytics.domain.model.BasheerEvent
import com.zeros.basheer.feature.analytics.domain.model.LearningSignal

interface AnalyticsRepository {

    /**
     * Stable anonymous identifier for this install.
     * Generated once on first use, persisted in SharedPreferences.
     * Used as both the analytics document path and the users/ document ID.
     */
    val installId: String

    /**
     * Enqueue a single behavioural event to the local Room queue.
     * This is fire-and-forget — the caller does not wait for Firestore.
     */
    suspend fun enqueue(event: BasheerEvent)

    /**
     * Enqueue a single learning-quality signal to the local Room queue.
     * Signals (correct and wrong answers, skips, unanswered exam questions) share the
     * same Room table and Firestore sync path as events — only the [eventType]
     * discriminator differs (e.g. "CheckpointAttempted", "PracticeQuestionAnswered").
     *
     * Fire-and-forget — same contract as [enqueue].
     */
    suspend fun enqueueSignal(signal: LearningSignal)

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