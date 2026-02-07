package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.UserProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM user_progress WHERE lessonId = :lessonId")
    fun getProgressByLesson(lessonId: String): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE lessonId = :lessonId")
    suspend fun getProgressByLessonOnce(lessonId: String): UserProgress?

    @Query("SELECT * FROM user_progress WHERE completed = 1")
    fun getCompletedLessons(): Flow<List<UserProgress>>

    @Query("SELECT COUNT(*) FROM user_progress WHERE completed = 1")
    fun getCompletedLessonsCount(): Flow<Int>

    @Query("""
        SELECT up.* FROM user_progress up
        INNER JOIN lessons l ON up.lessonId = l.id
        INNER JOIN units u ON l.unitId = u.id
        WHERE u.subjectId = :subjectId AND up.completed = 1
    """)
    fun getCompletedLessonsBySubject(subjectId: String): Flow<List<UserProgress>>

    @Query("SELECT * FROM user_progress ORDER BY lastAccessedAt DESC LIMIT :limit")
    fun getRecentlyAccessedLessons(limit: Int = 5): Flow<List<UserProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: UserProgress)

    @Update
    suspend fun updateProgress(progress: UserProgress)


    @Query("DELETE FROM user_progress WHERE lessonId = :lessonId")
    suspend fun deleteProgress(lessonId: String)

    @Query("DELETE FROM user_progress")
    suspend fun deleteAllProgress()

    @Transaction
    suspend fun markLessonCompleted(lessonId: String) {
        val existing = getProgressByLessonOnce(lessonId)
        if (existing != null) {
            updateProgress(
                existing.copy(
                    completed = true,
                    completedAt = System.currentTimeMillis()
                )
            )
        } else {
            insertProgress(
                UserProgress(
                    lessonId = lessonId,
                    completed = true,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
    }

    @Transaction
    suspend fun markSectionCompleted(lessonId: String, sectionId: String) {
        val existing = getProgressByLessonOnce(lessonId)
        if (existing != null) {
            val sections = existing.completedSections.split(",").filter { it.isNotEmpty() }.toMutableSet()
            sections.add(sectionId)
            updateProgress(existing.copy(completedSections = sections.joinToString(",")))
        } else {
            insertProgress(
                UserProgress(
                    lessonId = lessonId,
                    completedSections = sectionId
                )
            )
        }
    }
    @Transaction
    suspend fun updateProgressFromSections(lessonId: String, totalSections: Int) {
        val existing = getProgressByLessonOnce(lessonId) ?: return

        val completedCount = existing.completedSections
            .split(",")
            .filter { it.isNotEmpty() }
            .size

        val calculatedProgress = if (totalSections > 0) {
            completedCount.toFloat() / totalSections
        } else {
            0f
        }

        updateProgress(
            existing.copy(
                progress = calculatedProgress,
                completed = calculatedProgress >= 1.0f
            )
        )
    }
}
