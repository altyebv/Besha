package com.zeros.basheer.feature.feed.domain.repository


import com.zeros.basheer.feature.feed.data.dao.ContentVariantDao
import com.zeros.basheer.feature.feed.data.dao.FeedItemDao
import com.zeros.basheer.feature.feed.data.entity.ContentVariantEntity
import com.zeros.basheer.feature.feed.data.entity.FeedItemEntity
import com.zeros.basheer.feature.feed.domain.model.*
import com.zeros.basheer.feature.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val feedItemDao: FeedItemDao,
    private val contentVariantDao: ContentVariantDao
) : FeedRepository {

    // ===== Feed Items =====
    override suspend fun getFeedItemById(id: String): FeedItem? =
        feedItemDao.getFeedItemById(id)?.let { feedItemEntityToDomain(it) }

    override fun getFeedItemsByConcept(conceptId: String): Flow<List<FeedItem>> =
        feedItemDao.getFeedItemsByConcept(conceptId).map { entities ->
            entities.map { feedItemEntityToDomain(it) }
        }

    override fun getFeedItemsBySubject(subjectId: String): Flow<List<FeedItem>> =
        feedItemDao.getFeedItemsBySubject(subjectId).map { entities ->
            entities.map { feedItemEntityToDomain(it) }
        }

    override fun getFeedItemsByType(type: String): Flow<List<FeedItem>> =
        feedItemDao.getFeedItemsByType(type).map { entities ->
            entities.map { feedItemEntityToDomain(it) }
        }

    override fun getFeedItemsDueForReview(currentTime: Long, limit: Int): Flow<List<FeedItem>> =
        feedItemDao.getFeedItemsDueForReview(currentTime, limit).map { entities ->
            entities.map { feedItemEntityToDomain(it) }
        }

    override fun getFeedItemsForLearnedConcepts(subjectId: String, limit: Int): Flow<List<FeedItem>> =
        feedItemDao.getFeedItemsForLearnedConcepts(subjectId, limit).map { entities ->
            entities.map { feedItemEntityToDomain(it) }
        }

    override fun getHighPriorityFeedItems(subjectId: String, limit: Int): Flow<List<FeedItem>> =
        feedItemDao.getHighPriorityFeedItems(subjectId, limit).map { entities ->
            entities.map { feedItemEntityToDomain(it) }
        }

    override fun getRandomMiniQuizzes(subjectId: String, limit: Int): Flow<List<FeedItem>> =
        feedItemDao.getRandomMiniQuizzes(subjectId, limit).map { entities ->
            entities.map { feedItemEntityToDomain(it) }
        }

    override suspend fun insertFeedItem(feedItem: FeedItem) {
        feedItemDao.insertFeedItem(feedItemDomainToEntity(feedItem))
    }

    override suspend fun insertFeedItems(feedItems: List<FeedItem>) {
        feedItemDao.insertFeedItems(feedItems.map { feedItemDomainToEntity(it) })
    }

    override suspend fun updateFeedItem(feedItem: FeedItem) {
        feedItemDao.updateFeedItem(feedItemDomainToEntity(feedItem))
    }

    override suspend fun deleteFeedItem(feedItem: FeedItem) {
        feedItemDao.deleteFeedItem(feedItemDomainToEntity(feedItem))
    }

    override suspend fun deleteFeedItemsByConcept(conceptId: String) {
        feedItemDao.deleteFeedItemsByConcept(conceptId)
    }

    override suspend fun deleteFeedItemsBySubject(subjectId: String) {
        feedItemDao.deleteFeedItemsBySubject(subjectId)
    }

    override suspend fun deleteAllFeedItems() {
        feedItemDao.deleteAllFeedItems()
    }

    override fun getFeedItemCountBySubject(subjectId: String): Flow<Int> =
        feedItemDao.getFeedItemCountBySubject(subjectId)

    override suspend fun getFeedItemCountByConcept(conceptId: String): Int =
        feedItemDao.getFeedItemCountByConcept(conceptId)

    // ===== Content Variants =====
    override suspend fun getVariantById(id: String): ContentVariant? =
        contentVariantDao.getVariantById(id)?.let { variantEntityToDomain(it) }

    override fun getVariantsByConcept(conceptId: String): Flow<List<ContentVariant>> =
        contentVariantDao.getVariantsByConcept(conceptId).map { entities ->
            entities.map { variantEntityToDomain(it) }
        }

    override fun getVariantsByConceptAndType(conceptId: String, type: String): Flow<List<ContentVariant>> =
        contentVariantDao.getVariantsByConceptAndType(conceptId, type).map { entities ->
            entities.map { variantEntityToDomain(it) }
        }

    override fun getVariantsBySource(source: String): Flow<List<ContentVariant>> =
        contentVariantDao.getVariantsBySource(source).map { entities ->
            entities.map { variantEntityToDomain(it) }
        }

    override fun getExplanationsForConcept(conceptId: String): Flow<List<ContentVariant>> =
        contentVariantDao.getExplanationsForConcept(conceptId).map { entities ->
            entities.map { variantEntityToDomain(it) }
        }

    override fun getExamplesForConcept(conceptId: String): Flow<List<ContentVariant>> =
        contentVariantDao.getExamplesForConcept(conceptId).map { entities ->
            entities.map { variantEntityToDomain(it) }
        }

    override fun getMemoryAidsForConcept(conceptId: String): Flow<List<ContentVariant>> =
        contentVariantDao.getMemoryAidsForConcept(conceptId).map { entities ->
            entities.map { variantEntityToDomain(it) }
        }

    override fun getVerifiedTeacherContent(limit: Int): Flow<List<ContentVariant>> =
        contentVariantDao.getVerifiedTeacherContent(limit).map { entities ->
            entities.map { variantEntityToDomain(it) }
        }

    override fun getOfficialContentForConcept(conceptId: String): Flow<List<ContentVariant>> =
        contentVariantDao.getOfficialContentForConcept(conceptId).map { entities ->
            entities.map { variantEntityToDomain(it) }
        }

    override suspend fun insertVariant(variant: ContentVariant) {
        contentVariantDao.insertVariant(variantDomainToEntity(variant))
    }

    override suspend fun insertVariants(variants: List<ContentVariant>) {
        contentVariantDao.insertVariants(variants.map { variantDomainToEntity(it) })
    }

    override suspend fun updateVariant(variant: ContentVariant) {
        contentVariantDao.updateVariant(variantDomainToEntity(variant))
    }

    override suspend fun deleteVariant(variant: ContentVariant) {
        contentVariantDao.deleteVariant(variantDomainToEntity(variant))
    }

    override suspend fun deleteVariantsByConcept(conceptId: String) {
        contentVariantDao.deleteVariantsByConcept(conceptId)
    }

    override suspend fun deleteAllVariants() {
        contentVariantDao.deleteAllVariants()
    }

    override suspend fun upvoteVariant(variantId: String) {
        contentVariantDao.upvoteVariant(variantId)
    }

    override suspend fun downvoteVariant(variantId: String) {
        contentVariantDao.downvoteVariant(variantId)
    }

    override suspend fun verifyVariant(variantId: String) {
        contentVariantDao.verifyVariant(variantId)
    }

    override fun getUnverifiedVariants(): Flow<List<ContentVariant>> =
        contentVariantDao.getUnverifiedVariants().map { entities ->
            entities.map { variantEntityToDomain(it) }
        }

    override suspend fun getVariantCountByConcept(conceptId: String): Int =
        contentVariantDao.getVariantCountByConcept(conceptId)

    override suspend fun getVariantCountByConceptAndType(conceptId: String, type: String): Int =
        contentVariantDao.getVariantCountByConceptAndType(conceptId, type)

    // ===== Mappers =====
    private fun feedItemEntityToDomain(entity: FeedItemEntity): FeedItem = FeedItem(
        id = entity.id,
        conceptId = entity.conceptId,
        subjectId = entity.subjectId,
        type = FeedItemType.valueOf(entity.type),
        contentAr = entity.contentAr,
        contentEn = entity.contentEn,
        imageUrl = entity.imageUrl,
        interactionType = entity.interactionType?.let { InteractionType.valueOf(it) },
        correctAnswer = entity.correctAnswer,
        options = entity.options,
        explanation = entity.explanation,
        questionId = entity.questionId,
        priority = entity.priority,
        order = entity.order
    )

    private fun feedItemDomainToEntity(feedItem: FeedItem): FeedItemEntity = FeedItemEntity(
        id = feedItem.id,
        conceptId = feedItem.conceptId,
        subjectId = feedItem.subjectId,
        type = feedItem.type.name,
        contentAr = feedItem.contentAr,
        contentEn = feedItem.contentEn,
        imageUrl = feedItem.imageUrl,
        interactionType = feedItem.interactionType?.name,
        correctAnswer = feedItem.correctAnswer,
        options = feedItem.options,
        explanation = feedItem.explanation,
        questionId = feedItem.questionId,
        priority = feedItem.priority,
        order = feedItem.order
    )

    private fun variantEntityToDomain(entity: ContentVariantEntity): ContentVariant = ContentVariant(
        id = entity.id,
        conceptId = entity.conceptId,
        type = VariantType.valueOf(entity.type),
        source = ContentSource.valueOf(entity.source),
        contentAr = entity.contentAr,
        contentEn = entity.contentEn,
        imageUrl = entity.imageUrl,
        authorName = entity.authorName,
        authorTitle = entity.authorTitle,
        upvotes = entity.upvotes,
        order = entity.order,
        createdAt = entity.createdAt,
        isVerified = entity.isVerified
    )

    private fun variantDomainToEntity(variant: ContentVariant): ContentVariantEntity = ContentVariantEntity(
        id = variant.id,
        conceptId = variant.conceptId,
        type = variant.type.name,
        source = variant.source.name,
        contentAr = variant.contentAr,
        contentEn = variant.contentEn,
        imageUrl = variant.imageUrl,
        authorName = variant.authorName,
        authorTitle = variant.authorTitle,
        upvotes = variant.upvotes,
        order = variant.order,
        createdAt = variant.createdAt,
        isVerified = variant.isVerified
    )
}