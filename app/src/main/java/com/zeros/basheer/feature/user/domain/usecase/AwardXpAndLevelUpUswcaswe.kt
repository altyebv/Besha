package com.zeros.basheer.feature.user.domain.usecase

import com.zeros.basheer.feature.analytics.AnalyticsManager
import com.zeros.basheer.feature.user.domain.model.XpSource
import com.zeros.basheer.feature.user.domain.model.XpSummary
import com.zeros.basheer.feature.user.domain.model.XpTransaction
import com.zeros.basheer.feature.user.domain.repository.XpRepository
import javax.inject.Inject

/**
 * Awards XP and fires a level-up achievement notification if the award
 * pushed the user across a level boundary.
 *
 * Replaces direct [AwardXpUseCase] calls in ViewModels that should also
 * trigger the level-up notification. The level check lives here so it
 * never needs to be duplicated across LessonReaderViewModel,
 * PracticeSessionViewModel, and ExamSessionViewModel.
 *
 * Returns the same [XpTransaction]? that [AwardXpUseCase] returns so all
 * existing callers can keep reading tx?.amount for their UI state.
 */
class AwardXpAndCheckLevelUseCase @Inject constructor(
    private val xpRepository: XpRepository,
    private val analyticsManager: AnalyticsManager,
) {
    suspend operator fun invoke(
        source: XpSource,
        referenceId: String? = null,
    ): XpTransaction? {
        // Snapshot level BEFORE the award
        val xpBefore   = xpRepository.getTotalXp()
        val levelBefore = XpSummary.from(xpBefore).level

        // Perform the award (may return null if deduplicated)
        val tx = xpRepository.awardXp(source = source, referenceId = referenceId)
            ?: return null

        // Snapshot level AFTER
        val xpAfter    = xpRepository.getTotalXp()
        val levelAfter  = XpSummary.from(xpAfter).level

        // Fire notification only when a new level was reached
        if (levelAfter > levelBefore) {
            analyticsManager.xpLevelUp(
                newLevel  = levelAfter,
                totalXp   = xpAfter,
                xpSource  = source.name,
            )
        }

        return tx
    }
}