package com.zeros.basheer.feature.analytics.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persisted representation of a single [BasheerEvent].
 *
 * Lifecycle:
 *   1. Event fires → row inserted with synced = false
 *   2. WorkManager batch job → reads all unsynced rows
 *   3. Events serialized into one Firestore document per day
 *   4. On upload success → rows marked synced = true (or deleted)
 *
 * We keep synced rows for 30 days before pruning (useful for local debugging
 * and audit, cheap on storage since typical payload is <500 bytes).
 */
@Entity(
    tableName = "analytics_events",
    indices = [
        Index("synced"),          // Fast query for unsynced batch
        Index("dateBucket"),      // Group by day for batching
    ]
)
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Discriminator — the sealed class simple name, e.g. "LessonCompleted". */
    val eventType: String,

    /** JSON-serialized payload of the specific BasheerEvent subclass. */
    val payload: String,

    /** UTC epoch millis — set at enqueue time, not upload time. */
    val occurredAt: Long = System.currentTimeMillis(),

    /** "YYYY-MM-DD" bucket used to group events into one Firestore document. */
    val dateBucket: String,

    /**
     * Logical session identifier.
     * All events between AppSessionStarted and AppSessionEnded share the same value.
     * Format: "{installId}_{epochMillisOfSessionStart}"
     */
    val sessionId: String,

    /** Firestore upload status. false = pending, true = uploaded successfully. */
    val synced: Boolean = false,

    /** Epoch millis when this row was successfully uploaded. Null if not yet. */
    val syncedAt: Long? = null,
)