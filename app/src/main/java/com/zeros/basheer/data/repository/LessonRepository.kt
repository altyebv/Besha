package com.zeros.basheer.data.repository

import com.zeros.basheer.data.models.*
import com.zeros.basheer.feature.concept.domain.model.Concept
import com.zeros.basheer.feature.concept.domain.model.ConceptReview
import com.zeros.basheer.feature.concept.domain.model.Rating
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
import com.zeros.basheer.feature.feed.domain.model.FeedItem
import com.zeros.basheer.feature.feed.domain.repository.FeedRepository
import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import com.zeros.basheer.feature.streak.domain.model.StreakStatus
import com.zeros.basheer.feature.streak.domain.model.DailyActivity
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import com.zeros.basheer.feature.progress.domain.model.UserProgress
import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.subject.domain.model.Units
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepository @Inject constructor(
    private val progressRepository: ProgressRepository,  // NEW - replaced ProgressDao
    private val subjectRepository: SubjectRepository,
    private val conceptRepository: ConceptRepository,
    private val streakRepository: StreakRepository,
    private val feedRepository: FeedRepository
) {

    // Subjects
    fun getAllSubjects(): Flow<List<Subject>> = subjectRepository.getAllSubjects()

    fun getSubjectsByPath(path: StudentPath): Flow<List<Subject>> =
        subjectRepository.getSubjectsByPath(path)

    suspend fun getSubjectById(id: String): Subject? =
        subjectRepository.getSubjectById(id)

    // Units
    fun getUnitsBySubject(subjectId: String): Flow<List<Units>> =
        subjectRepository.getUnitsBySubject(subjectId)

    suspend fun getUnitById(id: String): Units? =
        subjectRepository.getUnitById(id)

    // Lessons
//    fun getLessonsByUnit(unitId: String): Flow<List<LessonEntity>> =
//        lessonRepository.getLessonsByUnit(unitId)
//
//    fun getLessonsBySubject(subjectId: String): Flow<List<LessonEntity>> =
//        lessonRepository.getLessonsBySubject(subjectId)
//
//     fun getLessonById(id: String): LessonEntity? =
//        lessonRepository.getLessonById(id)
//
//    fun getLessonByIdFlow(id: String): Flow<LessonEntity?> =
//        lessonRepository.getLessonByIdFlow(id)
//
//     fun getLessonFull(lessonId: String): com.zeros.basheer.data.relations.LessonFull? =
//        lessonRepository.getLessonFull(lessonId)
//
//    fun getLessonFullFlow(lessonId: String): Flow<com.zeros.basheer.data.relations.LessonFull?> =
//        lessonRepository.getLessonFullFlow(lessonId)

    suspend fun getConceptById(conceptId: String): Concept? =
        conceptRepository.getConceptById(conceptId)

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
//    suspend fun updateLessonProgress(lessonId: String) {
//        val lessonFull = lessonRepository.getLessonFull(lessonId)
//        val totalSections = lessonFull?.sections?.size ?: 1
//
//        val existing = progressRepository.getProgressByLessonOnce(lessonId) ?: return
//
//        val completedCount = existing.completedSections
//            .split(",")
//            .filter { it.isNotEmpty() }
//            .size
//
//        val calculatedProgress = if (totalSections > 0) {
//            completedCount.toFloat() / totalSections
//        } else {
//            0f
//        }
//
//        progressRepository.updateProgress(
//            existing.copy(
//                progress = calculatedProgress,
//                completed = calculatedProgress >= 1.0f
//            )
//        )
//    }

    // Concepts
    fun getConceptsBySubject(subjectId: String): Flow<List<Concept>> =
        conceptRepository.getConceptsBySubject(subjectId)

    fun getConceptsByLesson(lessonId: String): Flow<List<Concept>> =
        conceptRepository.getConceptsByLesson(lessonId)

    fun getNewConcepts(subjectId: String, limit: Int = 10): Flow<List<Concept>> =
        conceptRepository.getNewConcepts(subjectId, limit)

    // Concept Reviews
    fun getConceptsDueForReview(limit: Int = 20): Flow<List<ConceptReview>> =
        conceptRepository.getReviewsDueForReview(currentTime = System.currentTimeMillis(), limit = limit)

    suspend fun recordConceptReview(conceptId: String, rating: Rating) =
        conceptRepository.recordReview(conceptId, rating)

    // Feed Items
    fun getFeedItemsBySubject(subjectId: String): Flow<List<FeedItem>> =
        feedRepository.getFeedItemsBySubject(subjectId)

    fun getFeedItemsDueForReview(limit: Int = 20): Flow<List<FeedItem>> =
        feedRepository.getFeedItemsDueForReview(System.currentTimeMillis(), limit)

    fun getRandomMiniQuizzes(subjectId: String, limit: Int = 5): Flow<List<FeedItem>> =
        feedRepository.getRandomMiniQuizzes(subjectId, limit)

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