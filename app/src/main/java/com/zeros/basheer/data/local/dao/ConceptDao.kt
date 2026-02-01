package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.Concept
import com.zeros.basheer.data.models.ConceptType
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptDao {
    @Query("SELECT * FROM concepts WHERE subjectId = :subjectId ORDER BY title")
    fun getConceptsBySubject(subjectId: String): Flow<List<Concept>>

    @Query("SELECT * FROM concepts WHERE id = :conceptId")
    suspend fun getConceptById(conceptId: String): Concept?

    @Query("SELECT * FROM concepts WHERE type = :type ORDER BY title")
    fun getConceptsByType(type: ConceptType): Flow<List<Concept>>

    @Query("SELECT * FROM concepts WHERE tags LIKE '%' || :tag || '%'")
    fun getConceptsByTag(tag: String): Flow<List<Concept>>

    @Query("""
        SELECT * FROM concepts 
        WHERE relatedLessonIds LIKE '%' || :lessonId || '%'
    """)
    fun getConceptsByLesson(lessonId: String): Flow<List<Concept>>

    @Query("""
        SELECT c.* FROM concepts c
        WHERE c.subjectId = :subjectId
        AND c.id NOT IN (
            SELECT conceptId FROM concept_reviews
        )
        LIMIT :limit
    """)
    fun getNewConcepts(subjectId: String, limit: Int = 10): Flow<List<Concept>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcept(concept: Concept)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcepts(concepts: List<Concept>)

    @Delete
    suspend fun deleteConcept(concept: Concept)

    @Query("DELETE FROM concepts WHERE subjectId = :subjectId")
    suspend fun deleteConceptsBySubject(subjectId: String)
}