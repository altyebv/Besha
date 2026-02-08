package com.zeros.basheer.feature.progress.domain.usecase

import com.zeros.basheer.feature.progress.domain.model.UserProgress
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting progress for a specific lesson.
 */
class GetProgressByLessonUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    /**
     * Get progress as Flow (reactive).
     */
    operator fun invoke(lessonId: String): Flow<UserProgress?> {
        return repository.getProgressByLesson(lessonId)
    }

    /**
     * Get progress once (non-reactive).
     */
    suspend fun once(lessonId: String): UserProgress? {
        return repository.getProgressByLessonOnce(lessonId)
    }
}