package com.zeros.basheer.feature.progress.data.dao


import androidx.room.*
import com.zeros.basheer.feature.progress.data.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    // ==================== Queries ====================

    @Query("SELECT * FROM user_progress WHERE lessonId = :lessonId")
    fun getProgressByLesson(lessonId: String): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE lessonId = :lessonId")
    suspend fun getProgressByLessonOnce(lessonId: String): UserProgressEntity?

    @Query("SELECT * FROM user_progress WHERE completed = 1")
    fun getCompletedLessons(): Flow<List<UserProgressEntity>>

    @Query("SELECT COUNT(*) FROM user_progress WHERE completed = 1")
    fun getCompletedLessonsCount(): Flow<Int>

    @Query("""
        SELECT up.* FROM user_progress up
        INNER JOIN lessons l ON up.lessonId = l.id
        INNER JOIN units u ON l.unitId = u.id
        WHERE u.subjectId = :subjectId AND up.completed = 1
    """)
    fun getCompletedLessonsBySubject(subjectId: String): Flow<List<UserProgressEntity>>

    @Query("SELECT * FROM user_progress ORDER BY lastAccessedAt DESC LIMIT :limit")
    fun getRecentlyAccessedLessons(limit: Int = 5): Flow<List<UserProgressEntity>>

    // ==================== Inserts/Updates ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: UserProgressEntity)

    @Update
    suspend fun updateProgress(progress: UserProgressEntity)

    @Query("DELETE FROM user_progress WHERE lessonId = :lessonId")
    suspend fun deleteProgress(lessonId: String)

    @Query("DELETE FROM user_progress")
    suspend fun deleteAllProgress()

    // ==================== Transactions ====================

    /**
     * Marks a lesson as completed.
     * Creates progress record if it doesn't exist.
     */
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
                UserProgressEntity(
                    lessonId = lessonId,
                    completed = true,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Marks a section as completed within a lesson.
     * Updates the completed sections list.
     */
    @Transaction
    suspend fun markSectionCompleted(lessonId: String, sectionId: String) {
        val existing = getProgressByLessonOnce(lessonId)
        if (existing != null) {
            val sections = existing.completedSections
                .split(",")
                .filter { it.isNotEmpty() }
                .toMutableSet()
            sections.add(sectionId)
            updateProgress(existing.copy(completedSections = sections.joinToString(",")))
        } else {
            insertProgress(
                UserProgressEntity(
                    lessonId = lessonId,
                    completedSections = sectionId
                )
            )
        }
    }

    /**
     * Updates progress percentage based on completed sections.
     * Automatically marks lesson complete if all sections done.
     */
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