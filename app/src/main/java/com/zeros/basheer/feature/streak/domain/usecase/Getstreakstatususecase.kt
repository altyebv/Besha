package com.zeros.basheer.feature.streak.domain.usecase

import com.zeros.basheer.feature.streak.domain.model.StreakStatus
import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting the current streak status.
 * Returns current streak, longest streak, today's level, and risk status.
 */
class GetStreakStatusUseCase @Inject constructor(
    private val repository: StreakRepository
) {
    /**
     * Get streak status once.
     */
    suspend operator fun invoke(): StreakStatus {
        return repository.getStreakStatus()
    }

    /**
     * Get streak status as Flow for reactive UI.
     */
    fun asFlow(): Flow<StreakStatus> {
        return repository.getStreakStatusFlow()
    }
}