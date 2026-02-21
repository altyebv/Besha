package com.zeros.basheer.feature.user.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zeros.basheer.feature.user.data.entity.XpTransactionEntity
import com.zeros.basheer.feature.user.domain.model.XpSource
import kotlinx.coroutines.flow.Flow

@Dao
interface XpDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: XpTransactionEntity): Long

    /** Total XP across all time — reactive. */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM xp_transactions")
    fun observeTotalXp(): Flow<Int>

    /** One-shot total for non-reactive contexts. */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM xp_transactions")
    suspend fun getTotalXp(): Int

    /** XP earned today. */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM xp_transactions WHERE timestamp >= :startOfDay")
    suspend fun getXpToday(startOfDay: Long): Int

    /**
     * Check if a specific referenceId has already been awarded for a given source.
     * Used for deduplication — returns the count (0 or 1+).
     */
    @Query("SELECT COUNT(*) FROM xp_transactions WHERE source = :source AND referenceId = :referenceId")
    suspend fun countTransactions(source: XpSource, referenceId: String): Int

    /** Recent transactions for history/analytics. */
    @Query("SELECT * FROM xp_transactions ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentTransactions(limit: Int = 50): Flow<List<XpTransactionEntity>>
}