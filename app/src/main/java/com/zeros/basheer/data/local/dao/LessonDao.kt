package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.Lesson
import com.zeros.basheer.data.relations.LessonFull
import com.zeros.basheer.data.relations.LessonWithProgress
import com.zeros.basheer.data.relations.LessonWithSections
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE unitId = :unitId ORDER BY `order`")
    fun getLessonsByUnit(unitId: String): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): Lesson?

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun getLessonByIdFlow(lessonId: String): Flow<Lesson?>

    @Query("""
        SELECT l.* FROM lessons l
        INNER JOIN units u ON l.unitId = u.id
        WHERE u.subjectId = :subjectId
        ORDER BY u.`order`, l.`order`
    """)
    fun getLessonsBySubject(subjectId: String): Flow<List<Lesson>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: Lesson)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    @Delete
    suspend fun deleteLesson(lesson: Lesson)

    @Query("DELETE FROM lessons WHERE unitId = :unitId")
    suspend fun deleteLessonsByUnit(unitId: String)
}
