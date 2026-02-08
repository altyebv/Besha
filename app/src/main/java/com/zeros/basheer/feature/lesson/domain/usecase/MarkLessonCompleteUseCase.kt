package com.zeros.basheer.feature.lesson.domain.usecase


import com.zeros.basheer.core.domain.model.Result
import com.zeros.basheer.feature.lesson.domain.repository.LessonRepository
import javax.inject.Inject

class MarkLessonCompleteUseCase @Inject constructor(
    private val repository: LessonRepository
) {
    suspend operator fun invoke(lessonId: String): Result<Unit> {
        return repository.markLessonComplete(lessonId)
    }
}