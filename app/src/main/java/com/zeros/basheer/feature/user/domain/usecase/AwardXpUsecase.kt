package com.zeros.basheer.feature.user.domain.usecase

import com.zeros.basheer.feature.user.domain.model.XpSource
import com.zeros.basheer.feature.user.domain.model.XpTransaction
import com.zeros.basheer.feature.user.domain.repository.XpRepository
import javax.inject.Inject

/**
 * Single entry point for awarding XP across the app.
 *
 * Handles deduplication and streak multiplier internally —
 * callers just pass the source and referenceId.
 */
class AwardXpUseCase @Inject constructor(
    private val xpRepository: XpRepository
) {
    /**
     * @param source      What action triggered this award.
     * @param referenceId Unique ID for deduplication (lessonId, attemptId, sessionId).
     *                    Pass null for ephemeral actions like card reviews.
     * @return The transaction written, or null if skipped by deduplication.
     */
    suspend operator fun invoke(
        source: XpSource,
        referenceId: String? = null
    ): XpTransaction? = xpRepository.awardXp(
        source = source,
        referenceId = referenceId
    )
}