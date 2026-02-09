package com.zeros.basheer.feature.feed.domain.repository


import com.zeros.basheer.feature.feed.domain.model.FeedItem
import com.zeros.basheer.feature.feed.domain.model.ContentVariant
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    // Feed Items
    suspend fun getFeedItemById(id: String): FeedItem?
    fun getFeedItemsByConcept(conceptId: String): Flow<List<FeedItem>>
    fun getFeedItemsBySubject(subjectId: String): Flow<List<FeedItem>>
    fun getFeedItemsByType(type: String): Flow<List<FeedItem>>
    fun getFeedItemsDueForReview(currentTime: Long, limit: Int = 20): Flow<List<FeedItem>>
    fun getFeedItemsForLearnedConcepts(subjectId: String, limit: Int = 20): Flow<List<FeedItem>>
    fun getHighPriorityFeedItems(subjectId: String, limit: Int = 10): Flow<List<FeedItem>>
    fun getRandomMiniQuizzes(subjectId: String, limit: Int = 5): Flow<List<FeedItem>>
    suspend fun insertFeedItem(feedItem: FeedItem)
    suspend fun insertFeedItems(feedItems: List<FeedItem>)
    suspend fun updateFeedItem(feedItem: FeedItem)
    suspend fun deleteFeedItem(feedItem: FeedItem)
    suspend fun deleteFeedItemsByConcept(conceptId: String)
    suspend fun deleteFeedItemsBySubject(subjectId: String)
    suspend fun deleteAllFeedItems()
    fun getFeedItemCountBySubject(subjectId: String): Flow<Int>
    suspend fun getFeedItemCountByConcept(conceptId: String): Int

    // Content Variants
    suspend fun getVariantById(id: String): ContentVariant?
    fun getVariantsByConcept(conceptId: String): Flow<List<ContentVariant>>
    fun getVariantsByConceptAndType(conceptId: String, type: String): Flow<List<ContentVariant>>
    fun getVariantsBySource(source: String): Flow<List<ContentVariant>>
    fun getExplanationsForConcept(conceptId: String): Flow<List<ContentVariant>>
    fun getExamplesForConcept(conceptId: String): Flow<List<ContentVariant>>
    fun getMemoryAidsForConcept(conceptId: String): Flow<List<ContentVariant>>
    fun getVerifiedTeacherContent(limit: Int = 50): Flow<List<ContentVariant>>
    fun getOfficialContentForConcept(conceptId: String): Flow<List<ContentVariant>>
    suspend fun insertVariant(variant: ContentVariant)
    suspend fun insertVariants(variants: List<ContentVariant>)
    suspend fun updateVariant(variant: ContentVariant)
    suspend fun deleteVariant(variant: ContentVariant)
    suspend fun deleteVariantsByConcept(conceptId: String)
    suspend fun deleteAllVariants()
    suspend fun upvoteVariant(variantId: String)
    suspend fun downvoteVariant(variantId: String)
    suspend fun verifyVariant(variantId: String)
    fun getUnverifiedVariants(): Flow<List<ContentVariant>>
    suspend fun getVariantCountByConcept(conceptId: String): Int
    suspend fun getVariantCountByConceptAndType(conceptId: String, type: String): Int
}