package com.zeros.basheer.feature.lesson.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.zeros.basheer.data.relations.SectionWithBlocks
import com.zeros.basheer.data.relations.SectionWithBlocksAndConcepts
import com.zeros.basheer.data.relations.SectionWithConcepts
import com.zeros.basheer.feature.lesson.data.entity.SectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {
    @Query("SELECT * FROM sections WHERE lessonId = :lessonId ORDER BY `order`")
    fun getSectionsByLesson(lessonId: String): Flow<List<SectionEntity>>

    @Query("SELECT * FROM sections WHERE id = :sectionId")
    suspend fun getSectionById(sectionId: String): SectionEntity?

    @Transaction
    @Query("SELECT * FROM sections WHERE id = :sectionId")
    suspend fun getSectionWithBlocks(sectionId: String): SectionWithBlocks?

    @Query("SELECT * FROM sections WHERE lessonId = :lessonId ORDER BY `order`")
    suspend fun getSectionsByLessonId(lessonId: String): List<SectionEntity>

    @Transaction
    @Query("SELECT * FROM sections WHERE id = :sectionId")
    suspend fun getSectionWithConcepts(sectionId: String): SectionWithConcepts?

    @Transaction
    @Query("SELECT * FROM sections WHERE id = :sectionId")
    suspend fun getSectionFull(sectionId: String): SectionWithBlocksAndConcepts?

    @Transaction
    @Query("SELECT * FROM sections WHERE lessonId = :lessonId ORDER BY `order`")
    fun getSectionsFullByLesson(lessonId: String): Flow<List<SectionWithBlocksAndConcepts>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSection(sectionEntity: SectionEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSections(sectionEntities: List<SectionEntity>)

    @Delete
    suspend fun deleteSection(sectionEntity: SectionEntity)

    @Query("DELETE FROM sections WHERE lessonId = :lessonId")
    suspend fun deleteSectionsByLesson(lessonId: String)
}