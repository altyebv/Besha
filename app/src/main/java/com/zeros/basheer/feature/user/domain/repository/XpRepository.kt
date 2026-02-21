package com.zeros.basheer.feature.user.domain.repository

import com.zeros.basheer.feature.user.domain.model.XpSource
import com.zeros.basheer.feature.user.domain.model.XpSummary
import com.zeros.basheer.feature.user.domain.model.XpTransaction
import kotlinx.coroutines.flow.Flow

interface XpRepository {

    /** Observe live XP summary (total, level, progress). */
    fun observeXpSummary(): Flow<XpSummary>

    /**
     * Award XP for an action.
     *
     * @param source     The action type — determines base XP.
     * @param referenceId  Unique ID of the entity (lessonId, sessionId…).
     *                   Used for deduplication. Pass null for non-deduplicatable actions.
     * @param streakMultiplier  Pass the current streak multiplier from StreakRepository.
     *
     * @return The [XpTransaction] that was written, or null if deduplicated/skipped.
     */
    suspend fun awardXp(
        source: XpSource,
        referenceId: String? = null,
        streakMultiplier: Float = 1.0f
    ): XpTransaction?

    /** Total XP as a one-shot. */
    suspend fun getTotalXp(): Int

    /** XP earned today. */
    suspend fun getXpToday(): Int

    /**
     * Check if a referenceId has already been awarded for a source.
     * Returns true if this exact action was already recorded.
     */
    suspend fun hasBeenAwarded(source: XpSource, referenceId: String): Boolean
}