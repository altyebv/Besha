package com.zeros.basheer.feature.progress.domain.repository


import com.zeros.basheer.feature.progress.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user progress management.
 * Follows clean architecture - domain layer doesn't know about Room.
 */
interface ProgressRepository {

    // ==================== Queries ====================

    /**
     * Gets progress for a specific lesson as Flow.
     */
    fun getProgressByLesson(lessonId: String): Flow<UserProgress?>

    /**
     * Gets progress for a specific lesson once.
     */
    suspend fun getProgressByLessonOnce(lessonId: String): UserProgress?

    /**
     * Gets all completed lessons.
     */
    fun getCompletedLessons(): Flow<List<UserProgress>>

    /**
     * Gets count of completed lessons.
     */
    fun getCompletedLessonsCount(): Flow<Int>

    /**
     * Gets completed lessons for a specific subject.
     */
    fun getCompletedLessonsBySubject(subjectId: String): Flow<List<UserProgress>>

    /**
     * Gets recently accessed lessons.
     */
    fun getRecentlyAccessedLessons(limit: Int = 5): Flow<List<UserProgress>>

    // ==================== Updates ====================

    /**
     * Updates progress record.
     */
    suspend fun updateProgress(progress: UserProgress)

    /**
     * Marks a lesson as completed.
     */
    suspend fun markLessonCompleted(lessonId: String)

    /**
     * Marks a section as completed within a lesson.
     */
    suspend fun markSectionCompleted(lessonId: String, sectionId: String)

    /**
     * Updates progress percentage based on completed sections.
     */
    suspend fun updateProgressFromSections(lessonId: String, totalSections: Int)

    // ==================== Deletes ====================

    /**
     * Deletes progress for a specific lesson.
     */
    suspend fun deleteProgress(lessonId: String)

    /**
     * Deletes all progress (use with caution).
     */
    suspend fun deleteAllProgress()
}