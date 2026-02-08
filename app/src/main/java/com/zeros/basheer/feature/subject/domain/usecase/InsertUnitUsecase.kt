package com.zeros.basheer.feature.subject.domain.usecase

import com.zeros.basheer.feature.subject.domain.model.Units
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import javax.inject.Inject

/**
 * Use case for inserting units (used by data seeding)
 */
class InsertUnitsUseCase @Inject constructor(
    private val repository: SubjectRepository
) {
    /**
     * Insert a single unit
     */
    suspend operator fun invoke(units: Units) {
        repository.insertUnit(units)
    }

    /**
     * Insert multiple units (batch operation)
     */
    suspend fun insertMany(units: List<Units>) {
        repository.insertUnits(units)
    }
}