package com.zeros.basheer.feature.lesson.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity
import com.zeros.basheer.feature.lesson.data.relations.LessonFull
import com.zeros.basheer.feature.lesson.data.relations.LessonWithProgress
import com.zeros.basheer.feature.lesson.data.relations.LessonWithSections
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

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun getLessonCount(): Int

    // IGNORE (not REPLACE): REPLACE deletes the existing row first, which triggers
    // the CASCADE on user_progress and wipes all completion data on every app restart.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLesson(lessonEntity: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLessons(lessonEntities: List<LessonEntity>)

    @Delete
    suspend fun deleteLesson(lessonEntity: LessonEntity)

    @Query("DELETE FROM lessons WHERE unitId = :unitId")
    suspend fun deleteLessonsByUnit(unitId: String)
}