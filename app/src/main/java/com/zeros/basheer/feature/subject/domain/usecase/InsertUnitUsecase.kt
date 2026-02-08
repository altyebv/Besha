package com.zeros.basheer.feature.subject.domain.usecase

import com.zeros.basheer.feature.subject.domain.model.Unit
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
    suspend operator fun invoke(unit: Unit) {
        repository.insertUnit(unit)
    }

    /**
     * Insert multiple units (batch operation)
     */
    suspend fun insertMany(units: List<Unit>) {
        repository.insertUnits(units)
    }
}