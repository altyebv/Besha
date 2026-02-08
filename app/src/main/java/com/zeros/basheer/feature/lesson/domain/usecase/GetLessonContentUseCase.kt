package com.zeros.basheer.feature.lesson.domain.usecase


import com.zeros.basheer.core.domain.model.Result
import com.zeros.basheer.domain.model.LessonContent
import com.zeros.basheer.feature.lesson.domain.model.LessonContentDomain
import com.zeros.basheer.feature.lesson.domain.repository.LessonRepository
import javax.inject.Inject

class GetLessonContentUseCase @Inject constructor(
    private val repository: LessonRepository
) {
    suspend operator fun invoke(lessonId: String): Result<LessonContent> {
        return repository.getLessonContent(lessonId)
    }
}