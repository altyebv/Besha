package com.zeros.basheer.feature.concept.domain.repository


import com.zeros.basheer.feature.concept.domain.model.Concept
import com.zeros.basheer.feature.concept.domain.model.ConceptReview
import com.zeros.basheer.feature.concept.domain.model.ConceptTag
import com.zeros.basheer.feature.concept.domain.model.Rating
import com.zeros.basheer.feature.concept.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface ConceptRepository {
    // Concepts
    fun getConceptsBySubject(subjectId: String): Flow<List<Concept>>
    suspend fun getConceptById(conceptId: String): Concept?
    fun getConceptsByType(type: String): Flow<List<Concept>>
    fun getConceptsByLesson(lessonId: String): Flow<List<Concept>>
    fun getNewConcepts(subjectId: String, limit: Int = 10): Flow<List<Concept>>
    suspend fun insertConcept(concept: Concept)
    suspend fun insertConcepts(concepts: List<Concept>)
    suspend fun deleteConcept(concept: Concept)
    suspend fun deleteConceptsBySubject(subjectId: String)

    // Reviews
    suspend fun getReviewByConcept(conceptId: String): ConceptReview?
    fun getReviewByConceptFlow(conceptId: String): Flow<ConceptReview?>
    fun getReviewsBySubject(subjectId: String): Flow<List<ConceptReview>>
    fun getReviewsDueForReview(currentTime: Long = System.currentTimeMillis(), limit: Int = 20): Flow<List<ConceptReview>>
    fun getReviewsDueCount(currentTime: Long = System.currentTimeMillis()): Flow<Int>
    suspend fun recordReview(conceptId: String, rating: Rating)
    suspend fun insertReview(review: ConceptReview)
    suspend fun deleteReview(review: ConceptReview)

    // Tags
    fun getAllTags(): Flow<List<Tag>>
    suspend fun getTagById(tagId: String): Tag?
    suspend fun insertTag(tag: Tag)
    suspend fun insertTags(tags: List<Tag>)
    suspend fun deleteTag(tag: Tag)

    // Concept Tags
    fun getConceptTags(conceptId: String): Flow<List<ConceptTag>>
    fun getConceptsByTag(tagId: String): Flow<List<Concept>>
    fun getTagsForConcept(conceptId: String): Flow<List<Tag>>
    suspend fun insertConceptTag(conceptTag: ConceptTag)
    suspend fun insertConceptTags(conceptTags: List<ConceptTag>)
    suspend fun deleteConceptTag(conceptTag: ConceptTag)
    suspend fun deleteConceptTagsByConceptId(conceptId: String)
}