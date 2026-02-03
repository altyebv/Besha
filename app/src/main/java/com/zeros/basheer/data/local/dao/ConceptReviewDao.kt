package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.ConceptReview
import com.zeros.basheer.data.models.Rating
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptReviewDao {
    @Query("SELECT * FROM concept_reviews WHERE conceptId = :conceptId")
    suspend fun getReviewByConcept(conceptId: String): ConceptReview?

    @Query("SELECT * FROM concept_reviews WHERE conceptId = :conceptId")
    fun getReviewByConceptFlow(conceptId: String): Flow<ConceptReview?>

    @Query("""
        SELECT cr.* FROM concept_reviews cr
        INNER JOIN concepts c ON cr.conceptId = c.id
        WHERE c.subjectId = :subjectId
        ORDER BY cr.lastReviewedAt DESC
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
    fun getConceptsDueCount(currentTime: Long = System.currentTimeMillis()): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ConceptReview)

    @Update
    suspend fun updateReview(review: ConceptReview)

    @Delete
    suspend fun deleteReview(review: ConceptReview)

    @Query("DELETE FROM concept_reviews WHERE conceptId = :conceptId")
    suspend fun deleteReviewByConcept(conceptId: String)

    /**
     * Record a review using SM-2 spaced repetition algorithm
     */
    @Transaction
    suspend fun recordReview(conceptId: String, rating: Rating) {
        val existing = getReviewByConcept(conceptId)
        val now = System.currentTimeMillis()
        
        if (existing != null) {
            // Update existing review
            val (newInterval, newEaseFactor) = calculateSM2(
                currentInterval = existing.intervalDays,
                easeFactor = existing.easeFactor,
                rating = rating
            )
            val nextReview = now + (newInterval * 24 * 60 * 60 * 1000L)
            
            updateReview(
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
            // Create new review
            val (newInterval, newEaseFactor) = calculateSM2(
                currentInterval = 0,
                easeFactor = 2.5f,
                rating = rating
            )
            val nextReview = now + (newInterval * 24 * 60 * 60 * 1000L)
            
            insertReview(
                ConceptReview(
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

    /**
     * SM-2 Algorithm implementation
     * Returns Pair(newInterval, newEaseFactor)
     */
    private fun calculateSM2(currentInterval: Int, easeFactor: Float, rating: Rating): Pair<Int, Float> {
        val quality = when (rating) {
            Rating.FORGOT -> 0
            Rating.HARD -> 3
            Rating.GOOD -> 4
            Rating.EASY -> 5
        }
        
        // Update ease factor
        var newEF = easeFactor + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f))
        if (newEF < 1.3f) newEF = 1.3f
        
        // Calculate new interval
        val newInterval = when {
            quality < 3 -> 1  // Reset to 1 day if forgot
            currentInterval == 0 -> 1
            currentInterval == 1 -> 6
            else -> (currentInterval * newEF).toInt()
        }
        
        return Pair(newInterval, newEF)
    }
}
