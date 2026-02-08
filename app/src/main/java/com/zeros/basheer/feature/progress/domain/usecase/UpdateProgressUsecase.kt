package com.zeros.basheer.feature.progress.domain.usecase


import com.zeros.basheer.feature.progress.domain.model.UserProgress
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import javax.inject.Inject

/**
 * Use case for updating user progress.
 */
class UpdateProgressUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(progress: UserProgress) {
        repository.updateProgress(progress)
    }
}