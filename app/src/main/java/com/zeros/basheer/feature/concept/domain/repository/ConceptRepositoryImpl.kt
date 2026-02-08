package com.zeros.basheer.feature.concept.domain.repository


import com.zeros.basheer.feature.concept.data.dao.ConceptDao
import com.zeros.basheer.feature.concept.data.dao.ConceptReviewDao
import com.zeros.basheer.feature.concept.data.dao.ConceptTagDao
import com.zeros.basheer.feature.concept.data.dao.TagDao
import com.zeros.basheer.feature.concept.data.entity.ConceptEntity
import com.zeros.basheer.feature.concept.data.entity.ConceptReviewEntity
import com.zeros.basheer.feature.concept.data.entity.ConceptTagEntity
import com.zeros.basheer.feature.concept.data.entity.TagEntity
import com.zeros.basheer.feature.concept.domain.model.Concept
import com.zeros.basheer.feature.concept.domain.model.ConceptReview
import com.zeros.basheer.feature.concept.domain.model.ConceptTag
import com.zeros.basheer.feature.concept.domain.model.ConceptType
import com.zeros.basheer.feature.concept.domain.model.Rating
import com.zeros.basheer.feature.concept.domain.model.Tag
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConceptRepositoryImpl @Inject constructor(
    private val conceptDao: ConceptDao,
    private val conceptReviewDao: ConceptReviewDao,
    private val conceptTagDao: ConceptTagDao,
    private val tagDao: TagDao
) : ConceptRepository {

    // ===== Concepts =====
    override fun getConceptsBySubject(subjectId: String): Flow<List<Concept>> =
        conceptDao.getConceptsBySubject(subjectId).map { entities ->
            entities.map { conceptEntityToDomain(it) }
        }

    override suspend fun getConceptById(conceptId: String): Concept? =
        conceptDao.getConceptById(conceptId)?.let { conceptEntityToDomain(it) }

    override fun getConceptsByType(type: String): Flow<List<Concept>> =
        conceptDao.getConceptsByType(type).map { entities ->
            entities.map { conceptEntityToDomain(it) }
        }

    override fun getConceptsByLesson(lessonId: String): Flow<List<Concept>> =
        conceptDao.getConceptsByLesson(lessonId).map { entities ->
            entities.map { conceptEntityToDomain(it) }
        }

    override fun getNewConcepts(subjectId: String, limit: Int): Flow<List<Concept>> =
        conceptDao.getNewConcepts(subjectId, limit).map { entities ->
            entities.map { conceptEntityToDomain(it) }
        }


    override suspend fun insertConcept(concept: Concept) {
        conceptDao.insertConcept(conceptDomainToEntity(concept))
    }

    override suspend fun insertConcepts(concepts: List<Concept>) {
        conceptDao.insertConcepts(concepts.map { conceptDomainToEntity(it) })
    }

    override suspend fun deleteConcept(concept: Concept) {
        conceptDao.deleteConcept(conceptDomainToEntity(concept))
    }

    override suspend fun deleteConceptsBySubject(subjectId: String) {
        conceptDao.deleteConceptsBySubject(subjectId)
    }

    // ===== Reviews =====
    override suspend fun getReviewByConcept(conceptId: String): ConceptReview? =
        conceptReviewDao.getReviewByConcept(conceptId)?.let { reviewEntityToDomain(it) }

    override fun getReviewByConceptFlow(conceptId: String): Flow<ConceptReview?> =
        conceptReviewDao.getReviewByConceptFlow(conceptId).map { it?.let { reviewEntityToDomain(it) } }

    override fun getReviewsBySubject(subjectId: String): Flow<List<ConceptReview>> =
        conceptReviewDao.getReviewsBySubject(subjectId).map { entities ->
            entities.map { reviewEntityToDomain(it) }
        }

    override fun getReviewsDueForReview(currentTime: Long, limit: Int): Flow<List<ConceptReview>> =
        conceptReviewDao.getConceptsDueForReview(currentTime, limit).map { entities ->
            entities.map { reviewEntityToDomain(it) }
        }

    override fun getReviewsDueCount(currentTime: Long): Flow<Int> =
        conceptReviewDao.getConceptsDueCount(currentTime)

    override suspend fun recordReview(conceptId: String, rating: Rating) {
        val existing = conceptReviewDao.getReviewByConcept(conceptId)
        val now = System.currentTimeMillis()

        if (existing != null) {
            val (newInterval, newEaseFactor) = calculateSM2(
                currentInterval = existing.intervalDays,
                easeFactor = existing.easeFactor,
                rating = rating
            )
            val nextReview = now + (newInterval * 24 * 60 * 60 * 1000L)

            conceptReviewDao.updateReview(
                existing.copy(
                    lastReviewedAt = now,
                    nextReviewAt = nextReview,
                    reviewCount = existing.reviewCount + 1,
                    correctCount = if (rating != Rating.FORGOT) existing.correctCount + 1 else existing.correctCount,
                    intervalDays = newInterval,
                    easeFactor = newEaseFactor
                )
            )
        } else {
            val (newInterval, newEaseFactor) = calculateSM2(
                currentInterval = 0,
                easeFactor = 2.5f,
                rating = rating
            )
            val nextReview = now + (newInterval * 24 * 60 * 60 * 1000L)

            conceptReviewDao.insertReview(
                ConceptReviewEntity(
                    conceptId = conceptId,
                    firstSeenAt = now,
                    lastReviewedAt = now,
                    nextReviewAt = nextReview,
                    reviewCount = 1,
                    correctCount = if (rating != Rating.FORGOT) 1 else 0,
                    intervalDays = newInterval,
                    easeFactor = newEaseFactor
                )
            )
        }
    }

    override suspend fun insertReview(review: ConceptReview) {
        conceptReviewDao.insertReview(reviewDomainToEntity(review))
    }

    override suspend fun deleteReview(review: ConceptReview) {
        conceptReviewDao.deleteReview(reviewDomainToEntity(review))
    }

    // ===== Tags =====
    override fun getAllTags(): Flow<List<Tag>> =
        tagDao.getAllTags().map { entities ->
            entities.map { tagEntityToDomain(it) }
        }

    override suspend fun getTagById(tagId: String): Tag? =
        tagDao.getTagById(tagId)?.let { tagEntityToDomain(it) }

    override suspend fun insertTag(tag: Tag) {
        tagDao.insertTag(tagDomainToEntity(tag))
    }

    override suspend fun insertTags(tags: List<Tag>) {
        tagDao.insertTags(tags.map { tagDomainToEntity(it) })
    }

    override suspend fun deleteTag(tag: Tag) {
        tagDao.deleteTag(tagDomainToEntity(tag))
    }

    // ===== Concept Tags =====
    override fun getConceptTags(conceptId: String): Flow<List<ConceptTag>> =
        conceptTagDao.getByConceptId(conceptId).map { entities ->
            entities.map { conceptTagEntityToDomain(it) }
        }

    override fun getConceptsByTag(tagId: String): Flow<List<Concept>> =
        conceptTagDao.getConceptsByTag(tagId).map { entities ->
            entities.map { conceptEntityToDomain(it) }
        }

    override fun getTagsForConcept(conceptId: String): Flow<List<Tag>> =
        conceptTagDao.getTagsForConcept(conceptId).map { entities ->
            entities.map { tagEntityToDomain(it) }
        }

    override suspend fun insertConceptTag(conceptTag: ConceptTag) {
        conceptTagDao.insert(conceptTagDomainToEntity(conceptTag))
    }

    override suspend fun insertConceptTags(conceptTags: List<ConceptTag>) {
        conceptTagDao.insertAll(conceptTags.map { conceptTagDomainToEntity(it) })
    }

    override suspend fun deleteConceptTag(conceptTag: ConceptTag) {
        conceptTagDao.delete(conceptTagDomainToEntity(conceptTag))
    }

    override suspend fun deleteConceptTagsByConceptId(conceptId: String) {
        conceptTagDao.deleteByConceptId(conceptId)
    }

    // ===== Mappers =====
    private fun conceptEntityToDomain(entity: ConceptEntity): Concept = Concept(
        id = entity.id,
        subjectId = entity.subjectId,
        type = ConceptType.valueOf(entity.type),
        titleAr = entity.titleAr,
        titleEn = entity.titleEn,
        definition = entity.definition,
        shortDefinition = entity.shortDefinition,
        formula = entity.formula,
        imageUrl = entity.imageUrl,
        difficulty = entity.difficulty,
        extraData = entity.extraData
    )

    private fun conceptDomainToEntity(concept: Concept): ConceptEntity = ConceptEntity(
        id = concept.id,
        subjectId = concept.subjectId,
        type = concept.type.name,
        titleAr = concept.titleAr,
        titleEn = concept.titleEn,
        definition = concept.definition,
        shortDefinition = concept.shortDefinition,
        formula = concept.formula,
        imageUrl = concept.imageUrl,
        difficulty = concept.difficulty,
        extraData = concept.extraData
    )

    private fun reviewEntityToDomain(entity: ConceptReviewEntity): ConceptReview = ConceptReview(
        conceptId = entity.conceptId,
        firstSeenAt = entity.firstSeenAt,
        lastReviewedAt = entity.lastReviewedAt,
        nextReviewAt = entity.nextReviewAt,
        reviewCount = entity.reviewCount,
        correctCount = entity.correctCount,
        intervalDays = entity.intervalDays,
        easeFactor = entity.easeFactor
    )

    private fun reviewDomainToEntity(review: ConceptReview): ConceptReviewEntity = ConceptReviewEntity(
        conceptId = review.conceptId,
        firstSeenAt = review.firstSeenAt,
        lastReviewedAt = review.lastReviewedAt,
        nextReviewAt = review.nextReviewAt,
        reviewCount = review.reviewCount,
        correctCount = review.correctCount,
        intervalDays = review.intervalDays,
        easeFactor = review.easeFactor
    )

    private fun tagEntityToDomain(entity: TagEntity): Tag = Tag(
        id = entity.id,
        nameAr = entity.nameAr,
        nameEn = entity.nameEn,
        color = entity.color
    )

    private fun tagDomainToEntity(tag: Tag): TagEntity = TagEntity(
        id = tag.id,
        nameAr = tag.nameAr,
        nameEn = tag.nameEn,
        color = tag.color
    )

    private fun conceptTagEntityToDomain(entity: ConceptTagEntity): ConceptTag = ConceptTag(
        conceptId = entity.conceptId,
        tagId = entity.tagId
    )

    private fun conceptTagDomainToEntity(conceptTag: ConceptTag): ConceptTagEntity = ConceptTagEntity(
        conceptId = conceptTag.conceptId,
        tagId = conceptTag.tagId
    )

    // ===== SM-2 Algorithm =====
    private fun calculateSM2(currentInterval: Int, easeFactor: Float, rating: Rating): Pair<Int, Float> {
        val quality = when (rating) {
            Rating.FORGOT -> 0
            Rating.HARD -> 3
            Rating.GOOD -> 4
            Rating.EASY -> 5
        }

        var newEF = easeFactor + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f))
        if (newEF < 1.3f) newEF = 1.3f

        val newInterval = when {
            quality < 3 -> 1
            currentInterval == 0 -> 1
            currentInterval == 1 -> 6
            else -> (currentInterval * newEF).toInt()
        }

        return Pair(newInterval, newEF)
    }
}