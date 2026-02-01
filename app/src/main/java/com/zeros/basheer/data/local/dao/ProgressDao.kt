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

    // Helper function to mark lesson as completed
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
}