package com.zeros.basheer.feature.progress.data.repository


import com.zeros.basheer.feature.progress.data.dao.ProgressDao
import com.zeros.basheer.feature.progress.data.entity.UserProgressEntity
import com.zeros.basheer.feature.progress.domain.model.UserProgress
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of ProgressRepository.
 * Handles mapping between entities and domain models.
 */
class ProgressRepositoryImpl @Inject constructor(
    private val progressDao: ProgressDao
) : ProgressRepository {

    // ==================== Queries ====================

    override fun getProgressByLesson(lessonId: String): Flow<UserProgress?> =
        progressDao.getProgressByLesson(lessonId).map { it?.toDomain() }

    override suspend fun getProgressByLessonOnce(lessonId: String): UserProgress? =
        progressDao.getProgressByLessonOnce(lessonId)?.toDomain()

    override fun getCompletedLessons(): Flow<List<UserProgress>> =
        progressDao.getCompletedLessons().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getCompletedLessonsCount(): Flow<Int> =
        progressDao.getCompletedLessonsCount()

    override fun getCompletedLessonsBySubject(subjectId: String): Flow<List<UserProgress>> =
        progressDao.getCompletedLessonsBySubject(subjectId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getRecentlyAccessedLessons(limit: Int): Flow<List<UserProgress>> =
        progressDao.getRecentlyAccessedLessons(limit).map { entities ->
            entities.map { it.toDomain() }
        }

    // ==================== Updates ====================

    override suspend fun updateProgress(progress: UserProgress) {
        progressDao.updateProgress(progress.toEntity())
    }

    override suspend fun markLessonCompleted(lessonId: String) {
        progressDao.markLessonCompleted(lessonId)
    }

    override suspend fun markSectionCompleted(lessonId: String, sectionId: String) {
        progressDao.markSectionCompleted(lessonId, sectionId)
    }

    override suspend fun updateProgressFromSections(lessonId: String, totalSections: Int) {
        progressDao.updateProgressFromSections(lessonId, totalSections)
    }

    // ==================== Deletes ====================

    override suspend fun deleteProgress(lessonId: String) {
        progressDao.deleteProgress(lessonId)
    }

    override suspend fun deleteAllProgress() {
        progressDao.deleteAllProgress()
    }

    // ==================== Mappers ====================

    /**
     * Maps entity to domain model.
     */
    private fun UserProgressEntity.toDomain(): UserProgress = UserProgress(
        lessonId = lessonId,
        completed = completed,
        lastAccessedAt = lastAccessedAt,
        completedAt = completedAt,
        completedSections = completedSections,
        timeSpentSeconds = timeSpentSeconds,
        notes = notes,
        progress = progress
    )

    /**
     * Maps domain model to entity.
     */
    private fun UserProgress.toEntity(): UserProgressEntity = UserProgressEntity(
        lessonId = lessonId,
        completed = completed,
        lastAccessedAt = lastAccessedAt,
        completedAt = completedAt,
        completedSections = completedSections,
        timeSpentSeconds = timeSpentSeconds,
        notes = notes,
        progress = progress
    )
}