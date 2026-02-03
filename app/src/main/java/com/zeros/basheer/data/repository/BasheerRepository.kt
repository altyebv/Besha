package com.zeros.basheer.data.repository


import com.zeros.basheer.data.local.dao.*
import com.zeros.basheer.data.models.*
import com.zeros.basheer.data.relations.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BasheerRepository @Inject constructor(
    private val subjectDao: SubjectDao,
    private val unitDao: UnitDao,
    private val lessonDao: LessonDao,
    private val sectionDao: SectionDao,
    private val blockDao: BlockDao,
    private val conceptDao: ConceptDao,
    private val tagDao: TagDao,
    private val conceptTagDao: ConceptTagDao,
    private val sectionConceptDao: SectionConceptDao,
    private val questionDao: QuestionDao,
    private val questionConceptDao: QuestionConceptDao,
    private val examDao: ExamDao,
    private val examQuestionDao: ExamQuestionDao,
    private val progressDao: ProgressDao,
    private val conceptReviewDao: ConceptReviewDao,
    private val quizAttemptDao: QuizAttemptDao,
    private val questionResponseDao: QuestionResponseDao
) {
    // ==========================================
    // SUBJECTS
    // ==========================================
    fun getAllSubjects(): Flow<List<Subject>> = subjectDao.getAllSubjects()
    fun getSubjectsByPath(path: StudentPath): Flow<List<Subject>> = subjectDao.getSubjectsByPath(path)
    suspend fun getSubjectById(id: String): Subject? = subjectDao.getSubjectById(id)

    // ==========================================
    // UNITS
    // ==========================================
    fun getUnitsBySubject(subjectId: String): Flow<List<Units>> = unitDao.getUnitsBySubject(subjectId)
    fun getUnitsWithLessons(subjectId: String): Flow<List<UnitWithLessons>> = unitDao.getUnitsWithLessons(subjectId)
    suspend fun getUnitById(id: String): Units? = unitDao.getUnitById(id)

    // ==========================================
    // LESSONS
    // ==========================================
    fun getLessonsByUnit(unitId: String): Flow<List<Lesson>> = lessonDao.getLessonsByUnit(unitId)
    fun getLessonsBySubject(subjectId: String): Flow<List<Lesson>> = lessonDao.getLessonsBySubject(subjectId)
    suspend fun getLessonById(id: String): Lesson? = lessonDao.getLessonById(id)
    fun getLessonByIdFlow(id: String): Flow<Lesson?> = lessonDao.getLessonByIdFlow(id)
    suspend fun getLessonFull(lessonId: String): LessonFull? = lessonDao.getLessonFull(lessonId)
    fun getLessonFullFlow(lessonId: String): Flow<LessonFull?> = lessonDao.getLessonFullFlow(lessonId)

    // ==========================================
    // SECTIONS & BLOCKS
    // ==========================================
    fun getSectionsByLesson(lessonId: String): Flow<List<Section>> = sectionDao.getSectionsByLesson(lessonId)
    fun getBlocksBySection(sectionId: String): Flow<List<Block>> = blockDao.getBlocksBySection(sectionId)

    // ==========================================
    // CONCEPTS
    // ==========================================
    fun getConceptsBySubject(subjectId: String): Flow<List<Concept>> = conceptDao.getConceptsBySubject(subjectId)
    fun getConceptsByLesson(lessonId: String): Flow<List<Concept>> = conceptDao.getConceptsByLesson(lessonId)
    fun getNewConcepts(subjectId: String, limit: Int = 10): Flow<List<Concept>> = conceptDao.getNewConcepts(subjectId, limit)
    suspend fun getConceptById(id: String): Concept? = conceptDao.getConceptById(id)
    suspend fun getConceptsDueForReview(limit: Int = 20): List<Concept> = conceptDao.getConceptsDueForReview(limit = limit)

    // ==========================================
    // TAGS
    // ==========================================
    fun getAllTags(): Flow<List<Tag>> = tagDao.getAllTags()
    fun getConceptsByTag(tagId: String): Flow<List<Concept>> = conceptTagDao.getConceptsByTag(tagId)

    // ==========================================
    // QUESTIONS
    // ==========================================
    fun getQuestionsBySubject(subjectId: String): Flow<List<Question>> = questionDao.getQuestionsBySubject(subjectId)
    fun getQuestionsByUnit(unitId: String): Flow<List<Question>> = questionDao.getQuestionsByUnit(unitId)
    fun getQuestionsByConcept(conceptId: String): Flow<List<Question>> = questionDao.getQuestionsByConcept(conceptId)
    suspend fun getQuestionsForConcepts(conceptIds: List<String>, limit: Int): List<Question> = 
        questionDao.getQuestionsForConcepts(conceptIds, limit)
    suspend fun getFilteredQuestions(
        subjectId: String,
        unitId: String? = null,
        type: QuestionType? = null,
        conceptId: String? = null,
        minDifficulty: Int? = null,
        maxDifficulty: Int? = null,
        limit: Int = 20
    ): List<Question> = questionDao.getFilteredQuestions(
        subjectId, unitId, type, conceptId, minDifficulty, maxDifficulty, limit
    )

    // ==========================================
    // EXAMS
    // ==========================================
    fun getExamsBySubject(subjectId: String): Flow<List<Exam>> = examDao.getExamsBySubject(subjectId)
    fun getExamsBySource(source: ExamSource): Flow<List<Exam>> = examDao.getExamsBySource(source)
    suspend fun getExamWithQuestions(examId: String): ExamWithQuestions? = examDao.getExamWithQuestions(examId)

    // ==========================================
    // PROGRESS
    // ==========================================
    fun getProgressByLesson(lessonId: String): Flow<UserProgress?> = progressDao.getProgressByLesson(lessonId)
    fun getCompletedLessons(): Flow<List<UserProgress>> = progressDao.getCompletedLessons()
    fun getCompletedLessonsCount(): Flow<Int> = progressDao.getCompletedLessonsCount()
    suspend fun markLessonCompleted(lessonId: String) = progressDao.markLessonCompleted(lessonId)
    suspend fun markSectionCompleted(lessonId: String, sectionId: String) = progressDao.markSectionCompleted(lessonId, sectionId)
    suspend fun updateProgress(progress: UserProgress) = progressDao.updateProgress(progress)

    // ==========================================
    // CONCEPT REVIEWS (Spaced Repetition)
    // ==========================================
    fun getConceptsDueForReviewFlow(limit: Int = 20): Flow<List<ConceptReview>> = 
        conceptReviewDao.getConceptsDueForReview(limit = limit)
    fun getConceptsDueCount(): Flow<Int> = conceptReviewDao.getConceptsDueCount()
    suspend fun recordConceptReview(conceptId: String, rating: Rating) = 
        conceptReviewDao.recordReview(conceptId, rating)

    // ==========================================
    // QUIZ ATTEMPTS
    // ==========================================
    fun getAttemptsByExam(examId: String): Flow<List<QuizAttempt>> = quizAttemptDao.getAttemptsByExam(examId)
    suspend fun startQuizAttempt(examId: String): Long = quizAttemptDao.insertAttempt(QuizAttempt(examId = examId))
    suspend fun completeQuizAttempt(attemptId: Long, score: Int, totalPoints: Int, timeSpentSeconds: Int) = 
        quizAttemptDao.completeAttempt(attemptId, score, totalPoints, timeSpentSeconds)
    suspend fun getAttemptWithResponses(attemptId: Long): QuizAttemptWithResponses? = 
        quizAttemptDao.getAttemptWithResponses(attemptId)

    // ==========================================
    // QUESTION RESPONSES
    // ==========================================
    suspend fun recordQuestionResponse(
        attemptId: Long,
        questionId: String,
        userAnswer: String,
        isCorrect: Boolean,
        pointsEarned: Int = 0,
        timeSpentSeconds: Int? = null
    ): Long = questionResponseDao.insertResponse(
        QuestionResponse(
            attemptId = attemptId,
            questionId = questionId,
            userAnswer = userAnswer,
            isCorrect = isCorrect,
            pointsEarned = pointsEarned,
            timeSpentSeconds = timeSpentSeconds
        )
    )
}
