package com.zeros.basheer.feature.subject.domain.usecase


import com.zeros.basheer.feature.subject.domain.model.Units
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving units
 */
class GetUnitsUseCase @Inject constructor(
    private val repository: SubjectRepository
) {
    /**
     * Get all units for a specific subject, ordered by display order
     */
    operator fun invoke(subjectId: String): Flow<List<Units>> =
        repository.getUnitsBySubject(subjectId)

    /**
     * Get a specific unit by ID
     */
    suspend fun byId(unitId: String): Units? =
        repository.getUnitById(unitId)
}