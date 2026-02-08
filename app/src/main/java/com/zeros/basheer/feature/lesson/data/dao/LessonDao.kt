package com.zeros.basheer.feature.lesson.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.zeros.basheer.data.relations.LessonFull
import com.zeros.basheer.data.relations.LessonWithProgress
import com.zeros.basheer.data.relations.LessonWithSections
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE unitId = :unitId ORDER BY `order`")
    fun getLessonsByUnit(unitId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): LessonEntity?

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun getLessonByIdFlow(lessonId: String): Flow<LessonEntity?>

    @Query("""
        SELECT l.* FROM lessons l
        INNER JOIN units u ON l.unitId = u.id
        WHERE u.subjectId = :subjectId
        ORDER BY u.`order`, l.`order`
    """)
    fun getLessonsBySubject(subjectId: String): Flow<List<LessonEntity>>

    @Transaction
    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonWithSections(lessonId: String): LessonWithSections?

    @Transaction
    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonFull(lessonId: String): LessonFull?

    @Transaction
    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun getLessonFullFlow(lessonId: String): Flow<LessonFull?>

    @Transaction
    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonWithProgress(lessonId: String): LessonWithProgress?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertLesson(lessonEntity: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertLessons(lessonEntities: List<LessonEntity>)

    @Delete
    suspend fun deleteLesson(lessonEntity: LessonEntity)

    @Query("DELETE FROM lessons WHERE unitId = :unitId")
    suspend fun deleteLessonsByUnit(unitId: String)
}