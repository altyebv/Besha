package com.zeros.basheer.feature.streak.domain.usecase


import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import javax.inject.Inject

/**
 * Use case for recording time spent studying.
 * Updates daily activity and recalculates streak level.
 */
class RecordTimeSpentUseCase @Inject constructor(
    private val repository: StreakRepository
) {
    suspend operator fun invoke(seconds: Long) {
        repository.recordTimeSpent(seconds)
    }
}