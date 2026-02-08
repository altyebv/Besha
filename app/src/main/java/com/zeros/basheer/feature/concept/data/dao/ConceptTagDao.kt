package com.zeros.basheer.feature.concept.data.dao


import androidx.room.*
import com.zeros.basheer.feature.concept.data.entity.ConceptEntity
import com.zeros.basheer.feature.concept.data.entity.ConceptTagEntity
import com.zeros.basheer.feature.concept.data.entity.TagEntity
import com.zeros.basheer.feature.concept.domain.model.ConceptTag
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptTagDao {
    @Query("SELECT * FROM concept_tags WHERE conceptId = :conceptId")
    fun getByConceptId(conceptId: String): Flow<List<ConceptTagEntity>>

    @Query("SELECT * FROM concept_tags WHERE tagId = :tagId")
    fun getByTagId(tagId: String): Flow<List<ConceptTagEntity>>

    @Query("""
        SELECT c.* FROM concepts c
        INNER JOIN concept_tags ct ON c.id = ct.conceptId
        WHERE ct.tagId = :tagId
    """)
    fun getConceptsByTag(tagId: String): Flow<List<ConceptEntity>>

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN concept_tags ct ON t.id = ct.tagId
        WHERE ct.conceptId = :conceptId
    """)
    fun getTagsForConcept(conceptId: String): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conceptTag: ConceptTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conceptTags: List<ConceptTagEntity>)

    @Delete
    suspend fun delete(conceptTag: ConceptTagEntity)

    @Query("DELETE FROM concept_tags WHERE conceptId = :conceptId")
    suspend fun deleteByConceptId(conceptId: String)
}