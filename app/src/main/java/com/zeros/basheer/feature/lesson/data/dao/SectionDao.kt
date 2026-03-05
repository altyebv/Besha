package com.zeros.basheer.feature.lesson.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.zeros.basheer.feature.lesson.data.entity.SectionEntity
import com.zeros.basheer.feature.lesson.data.relations.SectionWithBlocks
import com.zeros.basheer.feature.lesson.data.relations.SectionWithBlocksAndConcepts
import com.zeros.basheer.feature.lesson.data.relations.SectionWithConcepts
import kotlinx.coroutines.flow.Flow

/** Room projection used by [SectionDao.getPartCountsByLessonIds]. */
data class LessonPartCount(val lessonId: String, val partCount: Int)

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

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertSection(sectionEntity: SectionEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertSections(sectionEntities: List<SectionEntity>)

    /**
     * Updates structural fields that may change across content versions.
     * Safe to call on re-seed — never touches section_progress (no cascade).
     */
    @Query("UPDATE sections SET title = :title, `order` = :order, partIndex = :partIndex, learningType = :learningType WHERE id = :id")
    suspend fun updateSectionStructure(id: String, title: String, order: Int, partIndex: Int, learningType: String)

    @Delete
    suspend fun deleteSection(sectionEntity: SectionEntity)

    @Query("DELETE FROM sections WHERE lessonId = :lessonId")
    suspend fun deleteSectionsByLesson(lessonId: String)

    /**
     * Returns the number of distinct parts (partIndex values) for each lessonId
     * in the given list. Used to show part pills on the LessonsScreen.
     */
    @Query("""
        SELECT lessonId, COUNT(DISTINCT partIndex) AS partCount
        FROM sections
        WHERE lessonId IN (:lessonIds)
        GROUP BY lessonId
    """)
    suspend fun getPartCountsByLessonIds(lessonIds: List<String>): List<LessonPartCount>
}