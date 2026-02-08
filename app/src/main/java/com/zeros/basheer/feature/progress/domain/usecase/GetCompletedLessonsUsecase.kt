package com.zeros.basheer.feature.progress.domain.usecase

import com.zeros.basheer.feature.progress.domain.model.UserProgress
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting completed lessons.
 */
class GetCompletedLessonsUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    /**
     * Get all completed lessons.
     */
    operator fun invoke(): Flow<List<UserProgress>> {
        return repository.getCompletedLessons()
    }

    /**
     * Get completed lessons count.
     */
    fun count(): Flow<Int> {
        return repository.getCompletedLessonsCount()
    }

    /**
     * Get completed lessons for a specific subject.
     */
    fun bySubject(subjectId: String): Flow<List<UserProgress>> {
        return repository.getCompletedLessonsBySubject(subjectId)
    }
}