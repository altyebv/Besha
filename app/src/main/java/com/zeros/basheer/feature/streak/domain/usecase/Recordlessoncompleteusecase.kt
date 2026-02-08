package com.zeros.basheer.feature.streak.domain.usecase


import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import javax.inject.Inject

/**
 * Use case for recording a completed lesson.
 * Updates daily activity and recalculates streak level.
 */
class RecordLessonCompletedUseCase @Inject constructor(
    private val repository: StreakRepository
) {
    suspend operator fun invoke() {
        repository.recordLessonCompleted()
    }
}