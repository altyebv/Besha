package com.zeros.basheer.domain.repository

import com.zeros.basheer.core.domain.model.Result
import com.zeros.basheer.feature.lesson.data.dao.LessonDao
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity
import com.zeros.basheer.feature.lesson.domain.model.LessonDomain
import com.zeros.basheer.feature.lesson.domain.repository.LessonRepository
import com.zeros.basheer.feature.progress.domain.model.UserProgress
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.subject.domain.model.Units
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Facade repository that provides unified access to content across features.
 *
 * This is needed because RecommendationEngine needs to access data from multiple features
 * (subject, lesson, progress) but we want to maintain clean feature boundaries.
 *
 * This acts as an anti-corruption layer between the recommendation domain and feature domains.
 */
@Singleton
class ContentRepository @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val lessonDao: LessonDao,  // Direct DAO access for legacy methods
    private val progressRepository: ProgressRepository
) {

    // ==================== Subjects ====================

    fun getAllSubjects(): Flow<List<Subject>> =
        subjectRepository.getAllSubjects()

    suspend fun getSubjectById(subjectId: String): Subject? =
        subjectRepository.getSubjectById(subjectId)

    // ==================== Units ====================

    fun getUnitsBySubject(subjectId: String): Flow<List<Units>> =
        subjectRepository.getUnitsBySubject(subjectId)

    suspend fun getUnitById(unitId: String): Units? =
        subjectRepository.getUnitById(unitId)

    // ==================== Lessons ====================

    /**
     * Get lessons by unit.
     * Returns LessonEntity directly since RecommendationEngine needs access to all fields.
     */
    fun getLessonsByUnit(unitId: String): Flow<List<LessonEntity>> =
        lessonDao.getLessonsByUnit(unitId)

    /**
     * Get lessons by subject.
     */
    fun getLessonsBySubject(subjectId: String): Flow<List<LessonEntity>> =
        lessonDao.getLessonsBySubject(subjectId)

    /**
     * Get lesson by ID.
     * Returns LessonEntity for backward compatibility with RecommendationEngine.
     */
    suspend fun getLessonById(lessonId: String): LessonEntity? =
        lessonDao.getLessonById(lessonId)

    // ==================== Progress ====================

    /**
     * Get completed lessons.
     */
    fun getCompletedLessons(): Flow<List<UserProgress>> =
        progressRepository.getCompletedLessons()

    /**
     * Get recently accessed lessons.
     */
    fun getRecentlyAccessedLessons(limit: Int = 5): Flow<List<UserProgress>> =
        progressRepository.getRecentlyAccessedLessons(limit)

    /**
     * Get progress for a specific lesson.
     */
    fun getProgressByLesson(lessonId: String): Flow<UserProgress?> =
        progressRepository.getProgressByLesson(lessonId)

    // ==================== Convenience Methods ====================

    /**
     * Get subject name in Arabic.
     */
    suspend fun getSubjectNameAr(subjectId: String): String =
        getSubjectById(subjectId)?.nameAr ?: ""

    /**
     * Count completed lessons for a subject.
     */
    suspend fun getCompletedLessonCount(subjectId: String): Int {
        val lessons = getLessonsBySubject(subjectId).first()
        val completedLessons = getCompletedLessons().first()
        return completedLessons.count { progress ->
            lessons.any { it.id == progress.lessonId }
        }
    }
}
