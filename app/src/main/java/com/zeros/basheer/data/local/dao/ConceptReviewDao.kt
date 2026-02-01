package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.ConceptReview
import com.zeros.basheer.data.models.Rating
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptReviewDao {
    @Query("SELECT * FROM concept_reviews WHERE conceptId = :conceptId ORDER BY reviewedAt DESC")
    fun getReviewsByConceptId(conceptId: String): Flow<List<ConceptReview>>

    @Query("""
        SELECT cr.* FROM concept_reviews cr
        INNER JOIN concepts c ON cr.conceptId = c.id
        WHERE c.subjectId = :subjectId
        ORDER BY cr.reviewedAt DESC
    """)
    fun getReviewsBySubject(subjectId: String): Flow<List<ConceptReview>>

    @Query("""
        SELECT * FROM concept_reviews 
        WHERE nextReviewAt <= :currentTime
        ORDER BY nextReviewAt ASC
        LIMIT :limit
    """)
    fun getConceptsDueForReview(
        currentTime: Long = System.currentTimeMillis(),
        limit: Int = 20
    ): Flow<List<ConceptReview>>

    @Query("""
        SELECT COUNT(*) FROM concept_reviews 
        WHERE nextReviewAt <= :currentTime
    """)
    fun getConceptsDueCount(
        currentTime: Long = System.currentTimeMillis()
    ): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ConceptReview)

    @Update
    suspend fun updateReview(review: ConceptReview)

    @Delete
    suspend fun deleteReview(review: ConceptReview)

    @Query("DELETE FROM concept_reviews WHERE conceptId = :conceptId")
    suspend fun deleteReviewsByConcept(conceptId: String)

    // Helper function to record a new review with spaced repetition logic
    @Transaction
    suspend fun recordReview(conceptId: String, rating: Rating) {
        val lastReview = getLastReview(conceptId)
        val newInterval = calculateInterval(lastReview?.intervalDays ?: 0, rating)
        val nextReviewTime = System.currentTimeMillis() + (newInterval * 24 * 60 * 60 * 1000L)

        insertReview(
            ConceptReview(
                conceptId = conceptId,
                reviewedAt = System.currentTimeMillis(),
                userRating = rating,
                nextReviewAt = nextReviewTime,
                reviewCount = (lastReview?.reviewCount ?: 0) + 1,
                intervalDays = newInterval
            )
        )
    }

    @Query("SELECT * FROM concept_reviews WHERE conceptId = :conceptId ORDER BY reviewedAt DESC LIMIT 1")
    suspend fun getLastReview(conceptId: String): ConceptReview?

    // Simple spaced repetition algorithm (SM-2 simplified)
    private fun calculateInterval(currentInterval: Int, rating: Rating): Int {
        return when (rating) {
            Rating.FORGOT -> 1 // Review tomorrow
            Rating.HARD -> maxOf(1, (currentInterval * 1.2).toInt()) // 20% increase
            Rating.GOOD -> maxOf(1, (currentInterval * 2.5).toInt()) // 2.5x increase
            Rating.EASY -> maxOf(1, (currentInterval * 4).toInt()) // 4x increase
        }
    }
}