package com.zeros.basheer.feature.progress.domain.usecase


import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import javax.inject.Inject

/**
 * Use case for updating progress based on completed sections.
 * Automatically calculates progress percentage and marks complete if all sections done.
 */
class UpdateProgressFromSectionsUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(lessonId: String, totalSections: Int) {
        repository.updateProgressFromSections(lessonId, totalSections)
    }
}