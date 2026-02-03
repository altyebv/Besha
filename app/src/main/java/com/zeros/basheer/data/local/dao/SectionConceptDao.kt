package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.SectionConcept
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionConceptDao {
    @Query("SELECT * FROM section_concepts WHERE sectionId = :sectionId ORDER BY `order`")
    fun getBySectionId(sectionId: String): Flow<List<SectionConcept>>

    @Query("SELECT * FROM section_concepts WHERE conceptId = :conceptId")
    fun getByConceptId(conceptId: String): Flow<List<SectionConcept>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sectionConcept: SectionConcept)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sectionConcepts: List<SectionConcept>)

    @Delete
    suspend fun delete(sectionConcept: SectionConcept)

    @Query("DELETE FROM section_concepts WHERE sectionId = :sectionId")
    suspend fun deleteBySectionId(sectionId: String)
}
