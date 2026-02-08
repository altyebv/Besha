package com.zeros.basheer.feature.concept.domain.usecase

import com.zeros.basheer.feature.concept.domain.model.Concept
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConceptsUseCase @Inject constructor(
    private val repository: ConceptRepository
) {
    operator fun invoke(subjectId: String): Flow<List<Concept>> =
        repository.getConceptsBySubject(subjectId)
}