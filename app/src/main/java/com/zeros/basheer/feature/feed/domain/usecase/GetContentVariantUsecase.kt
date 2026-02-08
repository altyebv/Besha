package com.zeros.basheer.feature.feed.domain.usecase


import com.zeros.basheer.feature.feed.domain.model.ContentVariant
import com.zeros.basheer.feature.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContentVariantsUseCase @Inject constructor(
    private val repository: FeedRepository
) {
    operator fun invoke(conceptId: String): Flow<List<ContentVariant>> =
        repository.getVariantsByConcept(conceptId)
}