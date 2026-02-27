package com.zeros.basheer.feature.analytics.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zeros.basheer.feature.analytics.data.entity.AnalyticsEventEntity

@Dao
interface AnalyticsEventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: AnalyticsEventEntity): Long

    /** Returns all unsynced events, oldest first — used by the sync worker. */
    @Query("SELECT * FROM analytics_events WHERE synced = 0 ORDER BY occurredAt ASC")
    suspend fun getUnsynced(): List<AnalyticsEventEntity>

    /**
     * Returns unsynced events grouped under a specific day bucket.
     * Useful when we want to finalize exactly one day's worth before uploading.
     */
    @Query("SELECT * FROM analytics_events WHERE synced = 0 AND dateBucket = :date ORDER BY occurredAt ASC")
    suspend fun getUnsyncedForDate(date: String): List<AnalyticsEventEntity>

    /** Returns all distinct unsynced date buckets — tells us which days need a Firestore write. */
    @Query("SELECT DISTINCT dateBucket FROM analytics_events WHERE synced = 0 ORDER BY dateBucket ASC")
    suspend fun getUnsyncedDateBuckets(): List<String>

    @Query("UPDATE analytics_events SET synced = 1, syncedAt = :now WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>, now: Long = System.currentTimeMillis())

    /**
     * Prune synced events older than [cutoffMillis].
     * Called by the sync worker after a successful upload to keep the table lean.
     */
    @Query("DELETE FROM analytics_events WHERE synced = 1 AND syncedAt < :cutoffMillis")
    suspend fun deleteSyncedBefore(cutoffMillis: Long)

    /** Total unsynced event count — useful for deciding whether to trigger early upload. */
    @Query("SELECT COUNT(*) FROM analytics_events WHERE synced = 0")
    suspend fun unsyncedCount(): Int
}