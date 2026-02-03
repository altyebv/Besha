package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.SectionProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionProgressDao {

    // ==================== Basic Queries ====================

    @Query("SELECT * FROM section_progress WHERE sectionId = :sectionId")
    suspend fun getProgressBySection(sectionId: String): SectionProgress?

    @Query("SELECT * FROM section_progress WHERE sectionId = :sectionId")
    fun getProgressBySectionFlow(sectionId: String): Flow<SectionProgress?>

    @Query("SELECT * FROM section_progress WHERE lessonId = :lessonId ORDER BY sectionId")
    fun getProgressByLesson(lessonId: String): Flow<List<SectionProgress>>

    @Query("SELECT * FROM section_progress WHERE lessonId = :lessonId")
    suspend fun getProgressByLessonOnce(lessonId: String): List<SectionProgress>

    // ==================== Completion Queries ====================

    @Query("SELECT * FROM section_progress WHERE lessonId = :lessonId AND completed = 1")
    fun getCompletedSectionsByLesson(lessonId: String): Flow<List<SectionProgress>>

    @Query("SELECT COUNT(*) FROM section_progress WHERE lessonId = :lessonId AND completed = 1")
    fun getCompletedSectionsCount(lessonId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM section_progress WHERE lessonId = :lessonId AND completed = 1")
    suspend fun getCompletedSectionsCountOnce(lessonId: String): Int

    /**
     * Check if all sections in a lesson are completed.
     * Returns true if completed sections count equals total sections count.
     */
    @Query("""
        SELECT CASE 
            WHEN (SELECT COUNT(*) FROM section_progress WHERE lessonId = :lessonId AND completed = 1) 
                 = (SELECT COUNT(*) FROM sections WHERE lessonId = :lessonId)
            THEN 1 ELSE 0 
        END
    """)
    suspend fun isLessonFullyCompleted(lessonId: String): Boolean

    // ==================== Resume Functionality ====================

    /**
     * Get the last accessed section in a lesson (for resume).
     */
    @Query("""
        SELECT * FROM section_progress 
        WHERE lessonId = :lessonId 
        ORDER BY lastAccessedAt DESC 
        LIMIT 1
    """)
    suspend fun getLastAccessedSection(lessonId: String): SectionProgress?

    /**
     * Get the first incomplete section in a lesson (for "continue" button).
     */
    @Query("""
        SELECT sp.* FROM section_progress sp
        INNER JOIN sections s ON sp.sectionId = s.id
        WHERE sp.lessonId = :lessonId AND sp.completed = 0
        ORDER BY s.`order` ASC
        LIMIT 1
    """)
    suspend fun getFirstIncompleteSection(lessonId: String): SectionProgress?

    // ==================== Time Tracking ====================

    @Query("SELECT SUM(timeSpentSeconds) FROM section_progress WHERE lessonId = :lessonId")
    fun getTotalTimeSpentOnLesson(lessonId: String): Flow<Int?>

    @Query("SELECT SUM(timeSpentSeconds) FROM section_progress")
    fun getTotalStudyTime(): Flow<Int?>

    // ==================== Insert/Update/Delete ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: SectionProgress)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressList(progressList: List<SectionProgress>)

    @Update
    suspend fun updateProgress(progress: SectionProgress)

    @Query("DELETE FROM section_progress WHERE sectionId = :sectionId")
    suspend fun deleteProgress(sectionId: String)

    @Query("DELETE FROM section_progress WHERE lessonId = :lessonId")
    suspend fun deleteProgressByLesson(lessonId: String)

    @Query("DELETE FROM section_progress")
    suspend fun deleteAllProgress()

    // ==================== Convenience Transactions ====================

    /**
     * Mark a section as completed.
     */
    @Transaction
    suspend fun markSectionCompleted(sectionId: String, lessonId: String) {
        val existing = getProgressBySection(sectionId)
        if (existing != null) {
            updateProgress(
                existing.copy(
                    completed = true,
                    completedAt = System.currentTimeMillis(),
                    lastAccessedAt = System.currentTimeMillis()
                )
            )
        } else {
            insertProgress(
                SectionProgress(
                    sectionId = sectionId,
                    lessonId = lessonId,
                    completed = true,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Update time spent on a section.
     */
    @Transaction
    suspend fun addTimeSpent(sectionId: String, lessonId: String, seconds: Int) {
        val existing = getProgressBySection(sectionId)
        if (existing != null) {
            updateProgress(
                existing.copy(
                    timeSpentSeconds = existing.timeSpentSeconds + seconds,
                    lastAccessedAt = System.currentTimeMillis()
                )
            )
        } else {
            insertProgress(
                SectionProgress(
                    sectionId = sectionId,
                    lessonId = lessonId,
                    timeSpentSeconds = seconds
                )
            )
        }
    }

    /**
     * Update scroll position for resume functionality.
     */
    @Transaction
    suspend fun updateScrollPosition(sectionId: String, lessonId: String, position: Float) {
        val existing = getProgressBySection(sectionId)
        if (existing != null) {
            updateProgress(
                existing.copy(
                    scrollPosition = position,
                    lastAccessedAt = System.currentTimeMillis()
                )
            )
        } else {
            insertProgress(
                SectionProgress(
                    sectionId = sectionId,
                    lessonId = lessonId,
                    scrollPosition = position
                )
            )
        }
    }
}