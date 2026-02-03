package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.Concept
import com.zeros.basheer.data.models.ConceptType
import com.zeros.basheer.data.relations.ConceptWithReview
import com.zeros.basheer.data.relations.ConceptWithSections
import com.zeros.basheer.data.relations.ConceptWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptDao {
    @Query("SELECT * FROM concepts WHERE subjectId = :subjectId ORDER BY titleAr")
    fun getConceptsBySubject(subjectId: String): Flow<List<Concept>>

    @Query("SELECT * FROM concepts WHERE id = :conceptId")
    suspend fun getConceptById(conceptId: String): Concept?

    @Query("SELECT * FROM concepts WHERE type = :type ORDER BY titleAr")
    fun getConceptsByType(type: ConceptType): Flow<List<Concept>>

    @Query("SELECT * FROM concepts WHERE subjectId = :subjectId AND type = :type ORDER BY titleAr")
    fun getConceptsBySubjectAndType(subjectId: String, type: ConceptType): Flow<List<Concept>>

    // Get concepts in a lesson via section_concepts junction
    @Query("""
        SELECT DISTINCT c.* FROM concepts c
        INNER JOIN section_concepts sc ON c.id = sc.conceptId
        INNER JOIN sections s ON sc.sectionId = s.id
        WHERE s.lessonId = :lessonId
        ORDER BY sc.`order`
    """)
    fun getConceptsByLesson(lessonId: String): Flow<List<Concept>>

    // Get concepts not yet reviewed (for new feed content)
    @Query("""
        SELECT c.* FROM concepts c
        WHERE c.subjectId = :subjectId
        AND c.id NOT IN (SELECT conceptId FROM concept_reviews)
        LIMIT :limit
    """)
    fun getNewConcepts(subjectId: String, limit: Int = 10): Flow<List<Concept>>

    // Get concepts due for review
    @Query("""
        SELECT c.* FROM concepts c
        INNER JOIN concept_reviews cr ON c.id = cr.conceptId
        WHERE cr.nextReviewAt <= :now
        ORDER BY cr.nextReviewAt ASC
        LIMIT :limit
    """)
    suspend fun getConceptsDueForReview(now: Long = System.currentTimeMillis(), limit: Int = 20): List<Concept>

    @Transaction
    @Query("SELECT * FROM concepts WHERE id = :conceptId")
    suspend fun getConceptWithSections(conceptId: String): ConceptWithSections?

    @Transaction
    @Query("SELECT * FROM concepts WHERE id = :conceptId")
    suspend fun getConceptWithTags(conceptId: String): ConceptWithTags?

    @Transaction
    @Query("SELECT * FROM concepts WHERE id = :conceptId")
    suspend fun getConceptWithReview(conceptId: String): ConceptWithReview?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcept(concept: Concept)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcepts(concepts: List<Concept>)

    @Delete
    suspend fun deleteConcept(concept: Concept)

    @Query("DELETE FROM concepts WHERE subjectId = :subjectId")
    suspend fun deleteConceptsBySubject(subjectId: String)
}
