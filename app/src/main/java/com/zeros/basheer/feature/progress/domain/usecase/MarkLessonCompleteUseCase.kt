package com.zeros.basheer.feature.progress.domain.usecase


import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import javax.inject.Inject

/**
 * Use case for marking a lesson as completed.
 */
class MarkLessonCompleteUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(lessonId: String) {
        repository.markLessonCompleted(lessonId)
    }
}