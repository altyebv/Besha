package com.zeros.basheer.feature.feed.domain.usecase


import com.zeros.basheer.feature.feed.domain.model.FeedItem
import com.zeros.basheer.feature.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFeedItemsUseCase @Inject constructor(
    private val repository: FeedRepository
) {
    operator fun invoke(subjectId: String): Flow<List<FeedItem>> =
        repository.getFeedItemsBySubject(subjectId)
}