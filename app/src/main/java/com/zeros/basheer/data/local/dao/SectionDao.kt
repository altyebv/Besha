package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.Section
import com.zeros.basheer.data.relations.SectionWithBlocks
import com.zeros.basheer.data.relations.SectionWithBlocksAndConcepts
import com.zeros.basheer.data.relations.SectionWithConcepts
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {
    @Query("SELECT * FROM sections WHERE lessonId = :lessonId ORDER BY `order`")
    fun getSectionsByLesson(lessonId: String): Flow<List<Section>>

    @Query("SELECT * FROM sections WHERE id = :sectionId")
    suspend fun getSectionById(sectionId: String): Section?

    @Transaction
    @Query("SELECT * FROM sections WHERE id = :sectionId")
    suspend fun getSectionWithBlocks(sectionId: String): SectionWithBlocks?

    @Transaction
    @Query("SELECT * FROM sections WHERE id = :sectionId")
    suspend fun getSectionWithConcepts(sectionId: String): SectionWithConcepts?

    @Transaction
    @Query("SELECT * FROM sections WHERE id = :sectionId")
    suspend fun getSectionFull(sectionId: String): SectionWithBlocksAndConcepts?

    @Transaction
    @Query("SELECT * FROM sections WHERE lessonId = :lessonId ORDER BY `order`")
    fun getSectionsFullByLesson(lessonId: String): Flow<List<SectionWithBlocksAndConcepts>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: Section)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<Section>)

    @Delete
    suspend fun deleteSection(section: Section)

    @Query("DELETE FROM sections WHERE lessonId = :lessonId")
    suspend fun deleteSectionsByLesson(lessonId: String)
}
