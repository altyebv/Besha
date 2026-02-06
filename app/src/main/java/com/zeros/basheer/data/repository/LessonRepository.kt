package com.zeros.basheer.data.repository

import com.zeros.basheer.data.local.dao.*
import com.zeros.basheer.data.models.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepository @Inject constructor(
    private val subjectDao: SubjectDao,
    private val unitDao: UnitDao,
    private val lessonDao: LessonDao,
    private val progressDao: ProgressDao,
    private val conceptDao: ConceptDao,
    private val conceptReviewDao: ConceptReviewDao,
    private val feedItemDao: FeedItemDao
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
    fun getLessonsByUnit(unitId: String): Flow<List<Lesson>> =
        lessonDao.getLessonsByUnit(unitId)

    fun getLessonsBySubject(subjectId: String): Flow<List<Lesson>> =
        lessonDao.getLessonsBySubject(subjectId)

    suspend fun getLessonById(id: String): Lesson? =
        lessonDao.getLessonById(id)

    fun getLessonByIdFlow(id: String): Flow<Lesson?> =
        lessonDao.getLessonByIdFlow(id)

    suspend fun getLessonFull(lessonId: String): com.zeros.basheer.data.relations.LessonFull? =
        lessonDao.getLessonFull(lessonId)

    fun getLessonFullFlow(lessonId: String): Flow<com.zeros.basheer.data.relations.LessonFull?> =
        lessonDao.getLessonFullFlow(lessonId)

    suspend fun getConceptById(conceptId: String): Concept? =
        conceptDao.getConceptById(conceptId)

    // Progress
    fun getProgressByLesson(lessonId: String): Flow<UserProgress?> =
        progressDao.getProgressByLesson(lessonId)

    fun getCompletedLessons(): Flow<List<UserProgress>> =
        progressDao.getCompletedLessons()

    fun getCompletedLessonsCount(): Flow<Int> =
        progressDao.getCompletedLessonsCount()

    suspend fun markLessonCompleted(lessonId: String) =
        progressDao.markLessonCompleted(lessonId)

    suspend fun updateProgress(progress: UserProgress) =
        progressDao.updateProgress(progress)

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
}