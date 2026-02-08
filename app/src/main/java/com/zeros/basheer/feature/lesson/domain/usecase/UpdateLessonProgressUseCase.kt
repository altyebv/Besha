package com.zeros.basheer.feature.lesson.domain.usecase


//import com.zeros.basheer.feature.lesson.domain.model.Lesson
import com.zeros.basheer.feature.lesson.domain.model.LessonDomain
import com.zeros.basheer.feature.lesson.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLessonsByUnitUseCase @Inject constructor(
    private val repository: LessonRepository
) {
    operator fun invoke(unitId: String): Flow<List<LessonDomain>> {
        return repository.getLessonsByUnit(unitId)
    }
}