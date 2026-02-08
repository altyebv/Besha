package com.zeros.basheer.feature.streak.domain.usecase

import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import javax.inject.Inject

/**
 * Use case for recording reviewed feed cards.
 * Updates daily activity and recalculates streak level.
 */
class RecordCardsReviewedUseCase @Inject constructor(
    private val repository: StreakRepository
) {
    suspend operator fun invoke(count: Int = 1) {
        repository.recordCardsReviewed(count)
    }
}