package com.zeros.basheer.feature.user.domain.usecase

import com.zeros.basheer.feature.user.domain.model.XpSummary
import com.zeros.basheer.feature.user.domain.repository.XpRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observe the user's full XP summary — total, level, and progress to next level.
 * Reactively updates after every XP award.
 */
class GetUserXpUseCase @Inject constructor(
    private val xpRepository: XpRepository
) {
    operator fun invoke(): Flow<XpSummary> = xpRepository.observeXpSummary()
}