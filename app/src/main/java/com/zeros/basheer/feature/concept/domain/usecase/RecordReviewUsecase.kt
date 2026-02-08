package com.zeros.basheer.feature.concept.domain.usecase


import com.zeros.basheer.feature.concept.domain.model.Rating
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
import javax.inject.Inject

class RecordReviewUseCase @Inject constructor(
    private val repository: ConceptRepository
) {
    suspend operator fun invoke(conceptId: String, rating: Rating) {
        repository.recordReview(conceptId, rating)
    }
}