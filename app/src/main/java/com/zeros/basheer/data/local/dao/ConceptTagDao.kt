package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.Concept
import com.zeros.basheer.data.models.ConceptTag
import com.zeros.basheer.data.models.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptTagDao {
    @Query("SELECT * FROM concept_tags WHERE conceptId = :conceptId")
    fun getByConceptId(conceptId: String): Flow<List<ConceptTag>>

    @Query("SELECT * FROM concept_tags WHERE tagId = :tagId")
    fun getByTagId(tagId: String): Flow<List<ConceptTag>>

    // Get concepts by tag
    @Query("""
        SELECT c.* FROM concepts c
        INNER JOIN concept_tags ct ON c.id = ct.conceptId
        WHERE ct.tagId = :tagId
    """)
    fun getConceptsByTag(tagId: String): Flow<List<Concept>>

    // Get tags for a concept
    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN concept_tags ct ON t.id = ct.tagId
        WHERE ct.conceptId = :conceptId
    """)
    fun getTagsForConcept(conceptId: String): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conceptTag: ConceptTag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conceptTags: List<ConceptTag>)

    @Delete
    suspend fun delete(conceptTag: ConceptTag)

    @Query("DELETE FROM concept_tags WHERE conceptId = :conceptId")
    suspend fun deleteByConceptId(conceptId: String)
}
