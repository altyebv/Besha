package com.zeros.basheer.feature.subject.domain.usecase


import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving subjects
 */
class GetSubjectsUseCase @Inject constructor(
    private val repository: SubjectRepository
) {
    /**
     * Get all subjects ordered by display order
     */
    operator fun invoke(): Flow<List<Subject>> =
        repository.getAllSubjects()

    /**
     * Get subjects filtered by student path (Science/Literary/Common)
     */
    fun byPath(path: StudentPath): Flow<List<Subject>> =
        repository.getSubjectsByPath(path)

    /**
     * Get a specific subject by ID
     */
    suspend fun byId(subjectId: String): Subject? =
        repository.getSubjectById(subjectId)
}