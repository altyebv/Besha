package com.zeros.basheer.feature.concept.data.dao


import androidx.room.*
import com.zeros.basheer.feature.concept.data.entity.ConceptReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptReviewDao {
    @Query("SELECT * FROM concept_reviews WHERE conceptId = :conceptId")
    suspend fun getReviewByConcept(conceptId: String): ConceptReviewEntity?

    @Query("SELECT * FROM concept_reviews WHERE conceptId = :conceptId")
    fun getReviewByConceptFlow(conceptId: String): Flow<ConceptReviewEntity?>

    @Query("""
        SELECT cr.* FROM concept_reviews cr
        INNER JOIN concepts c ON cr.conceptId = c.id
        WHERE c.subjectId = :subjectId
        ORDER BY cr.lastReviewedAt DESC
    """)
    fun getReviewsBySubject(subjectId: String): Flow<List<ConceptReviewEntity>>

    @Query("""
        SELECT * FROM concept_reviews 
        WHERE nextReviewAt <= :currentTime
        ORDER BY nextReviewAt ASC
        LIMIT :limit
    """)
    fun getConceptsDueForReview(
        currentTime: Long = System.currentTimeMillis(),
        limit: Int = 20
    ): Flow<List<ConceptReviewEntity>>

    @Query("""
        SELECT COUNT(*) FROM concept_reviews 
        WHERE nextReviewAt <= :currentTime
    """)
    fun getConceptsDueCount(currentTime: Long = System.currentTimeMillis()): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ConceptReviewEntity)

    @Update
    suspend fun updateReview(review: ConceptReviewEntity)

    @Delete
    suspend fun deleteReview(review: ConceptReviewEntity)

    @Query("DELETE FROM concept_reviews WHERE conceptId = :conceptId")
    suspend fun deleteReviewByConcept(conceptId: String)
}