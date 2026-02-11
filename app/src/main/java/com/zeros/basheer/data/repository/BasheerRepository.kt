package com.zeros.basheer.data.repository
//
//
import com.zeros.basheer.data.relations.*
import com.zeros.basheer.feature.concept.data.dao.ConceptReviewDao

import com.zeros.basheer.feature.concept.domain.model.Concept
import com.zeros.basheer.feature.concept.domain.model.Rating
import com.zeros.basheer.feature.concept.domain.model.Tag
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
import com.zeros.basheer.feature.lesson.data.dao.BlockDao
import com.zeros.basheer.feature.lesson.data.dao.LessonDao
import com.zeros.basheer.feature.lesson.data.dao.SectionDao
import com.zeros.basheer.feature.lesson.data.entity.BlockEntity
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity
import com.zeros.basheer.feature.lesson.data.entity.SectionEntity
import com.zeros.basheer.feature.progress.data.dao.ProgressDao
import com.zeros.basheer.feature.progress.data.entity.UserProgressEntity
import com.zeros.basheer.feature.quizbank.domain.model.Exam
import com.zeros.basheer.feature.quizbank.domain.model.ExamSource
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository

import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.subject.domain.model.Units
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BasheerRepository @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val conceptRepository: ConceptRepository,
    private val quizBankRepository: QuizBankRepository,
    private val lessonDao: LessonDao,
    private val sectionDao: SectionDao,
    private val blockDao: BlockDao,
    private val progressDao: ProgressDao,
    private val conceptReviewDao: ConceptReviewDao,
//    private val quizAttemptDao: QuizAttemptDao,
//    private val questionResponseDao: QuestionResponseDao
) {
    // ==========================================
    // SUBJECTS
    // ==========================================
    fun getAllSubjects(): Flow<List<Subject>> = subjectRepository.getAllSubjects()
    fun getSubjectsByPath(path: StudentPath): Flow<List<Subject>> = subjectRepository.getSubjectsByPath(path)
    suspend fun getSubjectById(id: String): Subject? = subjectRepository.getSubjectById(id)

    // ==========================================
    // UNITS
    // ==========================================
    fun getUnitsBySubject(subjectId: String): Flow<List<Units>> = subjectRepository.getUnitsBySubject(subjectId)
    // Note: getUnitsWithLessons moved to LessonRepository since it involves Lesson relations
    suspend fun getUnitById(id: String): Units? = subjectRepository.getUnitById(id)

    // ==========================================
    // LESSONS
    // ==========================================
    fun getLessonsByUnit(unitId: String): Flow<List<LessonEntity>> = lessonDao.getLessonsByUnit(unitId)
    fun getLessonsBySubject(subjectId: String): Flow<List<LessonEntity>> = lessonDao.getLessonsBySubject(subjectId)
    suspend fun getLessonById(id: String): LessonEntity? = lessonDao.getLessonById(id)
    fun getLessonByIdFlow(id: String): Flow<LessonEntity?> = lessonDao.getLessonByIdFlow(id)
    suspend fun getLessonFull(lessonId: String): LessonFull? = lessonDao.getLessonFull(lessonId)
    fun getLessonFullFlow(lessonId: String): Flow<LessonFull?> = lessonDao.getLessonFullFlow(lessonId)

    // ==========================================
    // SECTIONS & BLOCKS
    // ==========================================
    fun getSectionsByLesson(lessonId: String): Flow<List<SectionEntity>> = sectionDao.getSectionsByLesson(lessonId)
    fun getBlocksBySection(sectionId: String): Flow<List<BlockEntity>> = blockDao.getBlocksBySection(sectionId)

    // ==========================================
    // CONCEPTS
    // ==========================================
    fun getConceptsBySubject(subjectId: String): Flow<List<Concept>> = conceptRepository.getConceptsBySubject(subjectId)
    fun getConceptsByLesson(lessonId: String): Flow<List<Concept>> = conceptRepository.getConceptsByLesson(lessonId)
    fun getNewConcepts(subjectId: String, limit: Int = 10): Flow<List<Concept>> = conceptRepository.getNewConcepts(subjectId, limit)
    suspend fun getConceptById(id: String): Concept? = conceptRepository.getConceptById(id)

    // ==========================================
    // TAGS
    // ==========================================
    fun getAllTags(): Flow<List<Tag>> = conceptRepository.getAllTags()
    fun getConceptsByTag(tagId: String): Flow<List<Concept>> = conceptRepository.getConceptsByTag(tagId)

    // ==========================================
    // QUESTIONS
    // ==========================================
    fun getQuestionsBySubject(subjectId: String): Flow<List<Question>> = quizBankRepository.getQuestionsBySubject(subjectId)
    fun getQuestionsByUnit(unitId: String): Flow<List<Question>> = quizBankRepository.getQuestionsByUnit(unitId)
//    fun getQuestionsByConcept(conceptId: String): Flow<List<Question>> = quizBankRepository.getQuestionsByConcept(conceptId)
//    suspend fun getQuestionsForConcepts(conceptIds: List<String>, limit: Int): List<Question> =
//        quizBankRepository.getQuestionsForConcepts(conceptIds, limit)
//    suspend fun getFilteredQuestions(
//        subjectId: String,
//        unitId: String? = null,
//        type: QuestionType? = null,
//        conceptId: String? = null,
//        minDifficulty: Int? = null,
//        maxDifficulty: Int? = null,
//        limit: Int = 20
//    ): List<Question> = quizBankRepository.getFilteredQuestions(
//        subjectId, unitId, type, conceptId, minDifficulty, maxDifficulty, limit
//    )

    // ==========================================
    // EXAMS
    // ==========================================
    fun getExamsBySubject(subjectId: String): Flow<List<Exam>> = quizBankRepository.getExamsBySubject(subjectId)
//    fun getExamsBySource(source: ExamSource): Flow<List<Exam>> = quizBankRepository.getExamsBySource(source)
//    suspend fun getExamWithQuestions(examId: String): ExamWithQuestions? = quizBankRepository.getExamWithQuestions(examId)

    // ==========================================
    // PROGRESS
    // ==========================================
    fun getProgressByLesson(lessonId: String): Flow<UserProgressEntity?> = progressDao.getProgressByLesson(lessonId)
    fun getCompletedLessons(): Flow<List<UserProgressEntity>> = progressDao.getCompletedLessons()
    fun getCompletedLessonsCount(): Flow<Int> = progressDao.getCompletedLessonsCount()
    suspend fun markLessonCompleted(lessonId: String) = progressDao.markLessonCompleted(lessonId)
    suspend fun markSectionCompleted(lessonId: String, sectionId: String) = progressDao.markSectionCompleted(lessonId, sectionId)
    suspend fun updateProgress(progress: UserProgressEntity) = progressDao.updateProgress(progress)

    // ==========================================
    // CONCEPT REVIEWS (Spaced Repetition)
    // ==========================================
    fun getConceptsDueCount(): Flow<Int> = conceptReviewDao.getConceptsDueCount()
    suspend fun recordConceptReview(conceptId: String, rating: Rating) =
        conceptRepository.recordReview(conceptId, rating)

    // ==========================================
    // QUIZ ATTEMPTS
    // ==========================================
//    fun getAttemptsByExam(examId: String): Flow<List<QuizAttempt>> = quizBankRepository.getAttemptsByExam(examId)
//    suspend fun startQuizAttempt(examId: String): Long = quizBankRepository.insertAttempt(QuizAttempt(examId = examId))
//    suspend fun completeQuizAttempt(attemptId: Long, score: Int, totalPoints: Int, timeSpentSeconds: Int) =
//        quizBankRepository.completeAttempt(attemptId, score, totalPoints, timeSpentSeconds)
//    suspend fun getAttemptWithResponses(attemptId: Long): QuizAttemptWithResponses? =
//        quizAttemptDao.getAttemptWithResponses(attemptId)

    // ==========================================
    // QUESTION RESPONSES
    // ==========================================
//    suspend fun recordQuestionResponse(
//        attemptId: Long,
//        questionId: String,
//        userAnswer: String,
//        isCorrect: Boolean,
//        pointsEarned: Int = 0,
//        timeSpentSeconds: Int? = null
//    ): Long = quizBankRepository.insertResponse(
//        QuestionResponse(
//            attemptId = attemptId,
//            questionId = questionId,
//            userAnswer = userAnswer,
//            isCorrect = isCorrect,
//            pointsEarned = pointsEarned,
//            timeSpentSeconds = timeSpentSeconds
//        )
//    )
}