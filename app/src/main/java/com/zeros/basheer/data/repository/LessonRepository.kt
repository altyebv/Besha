package com.zeros.basheer.data.repository

import com.zeros.basheer.data.local.dao.*
import com.zeros.basheer.data.models.*
import com.zeros.basheer.feature.lesson.data.dao.LessonDao
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity
import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import com.zeros.basheer.feature.streak.domain.model.StreakStatus
import com.zeros.basheer.feature.streak.domain.model.DailyActivity
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import com.zeros.basheer.feature.progress.domain.model.UserProgress
import com.zeros.basheer.feature.subject.data.dao.SubjectDao
import com.zeros.basheer.feature.subject.data.dao.UnitDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepository @Inject constructor(
    private val subjectDao: SubjectDao,
    private val unitDao: UnitDao,
    private val lessonDao: LessonDao,
    private val progressRepository: ProgressRepository,  // NEW - replaced ProgressDao
    private val conceptDao: ConceptDao,
    private val conceptReviewDao: ConceptReviewDao,
    private val feedItemDao: FeedItemDao,
    private val streakRepository: StreakRepository
) {

    // Subjects
    fun getAllSubjects(): Flow<List<Subject>> = subjectDao.getAllSubjects()

    fun getSubjectsByPath(path: StudentPath): Flow<List<Subject>> =
        subjectDao.getSubjectsByPath(path)

    suspend fun getSubjectById(id: String): Subject? =
        subjectDao.getSubjectById(id)

    // Units
    fun getUnitsBySubject(subjectId: String): Flow<List<Units>> =
        unitDao.getUnitsBySubject(subjectId)

    suspend fun getUnitById(id: String): Units? =
        unitDao.getUnitById(id)

    // Lessons
    fun getLessonsByUnit(unitId: String): Flow<List<LessonEntity>> =
        lessonDao.getLessonsByUnit(unitId)

    fun getLessonsBySubject(subjectId: String): Flow<List<LessonEntity>> =
        lessonDao.getLessonsBySubject(subjectId)

    suspend fun getLessonById(id: String): LessonEntity? =
        lessonDao.getLessonById(id)

    fun getLessonByIdFlow(id: String): Flow<LessonEntity?> =
        lessonDao.getLessonByIdFlow(id)

    suspend fun getLessonFull(lessonId: String): com.zeros.basheer.data.relations.LessonFull? =
        lessonDao.getLessonFull(lessonId)

    fun getLessonFullFlow(lessonId: String): Flow<com.zeros.basheer.data.relations.LessonFull?> =
        lessonDao.getLessonFullFlow(lessonId)

    suspend fun getConceptById(conceptId: String): Concept? =
        conceptDao.getConceptById(conceptId)

    // Progress
    fun getProgressByLesson(lessonId: String): Flow<UserProgress?> =
        progressRepository.getProgressByLesson(lessonId)

    fun getCompletedLessons(): Flow<List<UserProgress>> =
        progressRepository.getCompletedLessons()

    fun getCompletedLessonsCount(): Flow<Int> =
        progressRepository.getCompletedLessonsCount()

    // ==================== ADD THIS METHOD ====================
    fun getRecentlyAccessedLessons(limit: Int = 5): Flow<List<UserProgress>> =
        progressRepository.getRecentlyAccessedLessons(limit)
    // ========================================================

    suspend fun markLessonCompleted(lessonId: String) =
        progressRepository.markLessonCompleted(lessonId)

    suspend fun updateProgress(progress: UserProgress) =
        progressRepository.updateProgress(progress)

    /**
     * Update lesson progress based on completed sections
     */
    suspend fun updateLessonProgress(lessonId: String) {
        val lessonFull = lessonDao.getLessonFull(lessonId)
        val totalSections = lessonFull?.sections?.size ?: 1

        val existing = progressRepository.getProgressByLessonOnce(lessonId) ?: return

        val completedCount = existing.completedSections
            .split(",")
            .filter { it.isNotEmpty() }
            .size

        val calculatedProgress = if (totalSections > 0) {
            completedCount.toFloat() / totalSections
        } else {
            0f
        }

        progressRepository.updateProgress(
            existing.copy(
                progress = calculatedProgress,
                completed = calculatedProgress >= 1.0f
            )
        )
    }

    // Concepts
    fun getConceptsBySubject(subjectId: String): Flow<List<Concept>> =
        conceptDao.getConceptsBySubject(subjectId)

    fun getConceptsByLesson(lessonId: String): Flow<List<Concept>> =
        conceptDao.getConceptsByLesson(lessonId)

    fun getNewConcepts(subjectId: String, limit: Int = 10): Flow<List<Concept>> =
        conceptDao.getNewConcepts(subjectId, limit)

    // Concept Reviews
    fun getConceptsDueForReview(limit: Int = 20): Flow<List<ConceptReview>> =
        conceptReviewDao.getConceptsDueForReview(limit = limit)

    suspend fun recordConceptReview(conceptId: String, rating: Rating) =
        conceptReviewDao.recordReview(conceptId, rating)

    // Feed Items
    fun getFeedItemsBySubject(subjectId: String): Flow<List<FeedItem>> =
        feedItemDao.getFeedItemsBySubject(subjectId)

    fun getFeedItemsDueForReview(limit: Int = 20): Flow<List<FeedItem>> =
        feedItemDao.getFeedItemsDueForReview(System.currentTimeMillis(), limit)

    fun getRandomMiniQuizzes(subjectId: String, limit: Int = 5): Flow<List<FeedItem>> =
        feedItemDao.getRandomMiniQuizzes(subjectId, limit)

    // ==================== Streak & Activity ====================

    fun getStreakStatusFlow(): Flow<StreakStatus> =
        streakRepository.getStreakStatusFlow()

    suspend fun getStreakStatus(): StreakStatus =
        streakRepository.getStreakStatus()

    fun getTodayActivityFlow(): Flow<DailyActivity?> =
        streakRepository.getTodayActivityFlow()

    fun getRecentActivity(days: Int = 30): Flow<List<DailyActivity>> =
        streakRepository.getRecentActivity(days)

    suspend fun recordLessonCompleted() {
        streakRepository.recordLessonCompleted()
    }

    suspend fun recordCardsReviewed(count: Int = 1) {
        streakRepository.recordCardsReviewed(count)
    }

    suspend fun recordQuestionsAnswered(count: Int = 1) {
        streakRepository.recordQuestionsAnswered(count)
    }

    suspend fun recordExamCompleted() {
        streakRepository.recordExamCompleted()
    }

    suspend fun recordTimeSpent(seconds: Long) {
        streakRepository.recordTimeSpent(seconds)
    }

    // Aggregate stats
    fun getTotalLessonsCompleted(): Flow<Int> = streakRepository.getTotalLessonsCompleted()
    fun getTotalCardsReviewed(): Flow<Int> = streakRepository.getTotalCardsReviewed()
    fun getTotalQuestionsAnswered(): Flow<Int> = streakRepository.getTotalQuestionsAnswered()
    fun getTotalTimeSpent(): Flow<Long> = streakRepository.getTotalTimeSpent()
}