package com.zeros.basheer.feature.progress.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zeros.basheer.feature.progress.data.entity.LessonPartProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonPartProgressDao {

    /** Check whether a specific part has been completed. */
    @Query("SELECT * FROM lesson_part_progress WHERE id = :id LIMIT 1")
    suspend fun getPartProgress(id: String): LessonPartProgressEntity?

    /** All completed parts for a lesson — used to check if lesson is fully done. */
    @Query("SELECT * FROM lesson_part_progress WHERE lessonId = :lessonId")
    suspend fun getCompletedPartsForLesson(lessonId: String): List<LessonPartProgressEntity>

    /** Reactive — lets LessonsScreen observe part progress per lesson. */
    @Query("SELECT * FROM lesson_part_progress WHERE lessonId = :lessonId")
    fun getCompletedPartsForLessonFlow(lessonId: String): Flow<List<LessonPartProgressEntity>>

    /** Completed part indices for a set of lessons — used to derive next-part in LessonsScreen. */
    @Query("SELECT * FROM lesson_part_progress WHERE lessonId IN (:lessonIds)")
    suspend fun getCompletedPartsForLessons(lessonIds: List<String>): List<LessonPartProgressEntity>

    /** How many distinct parts a lesson has (derived from sections table). */
    @Query("SELECT COUNT(DISTINCT partIndex) FROM sections WHERE lessonId = :lessonId")
    suspend fun getTotalPartCount(lessonId: String): Int

    /**
     * Insert with IGNORE — safe to call on re-completion.
     * A part is never un-completed, so we don't need REPLACE.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markPartComplete(entity: LessonPartProgressEntity)

    @Query("DELETE FROM lesson_part_progress WHERE lessonId = :lessonId")
    suspend fun deletePartsForLesson(lessonId: String)
}