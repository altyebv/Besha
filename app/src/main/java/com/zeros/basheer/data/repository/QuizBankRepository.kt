package com.zeros.basheer.data.repository


import com.zeros.basheer.data.local.dao.ConceptDao
import com.zeros.basheer.data.local.dao.ExamDao
import com.zeros.basheer.data.local.dao.ExamQuestionDao
import com.zeros.basheer.feature.lesson.data.dao.LessonDao
import com.zeros.basheer.data.local.dao.PracticeSessionDao
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import com.zeros.basheer.data.local.dao.QuestionConceptDao
import com.zeros.basheer.data.local.dao.QuestionDao
import com.zeros.basheer.data.local.dao.QuestionStatsDao
import com.zeros.basheer.data.local.dao.UnitDao
import com.zeros.basheer.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing the Quiz Bank.
 *
 * Provides access patterns for:
 * - Full exams (ministry, school, etc.)
 * - Filtered question queries (by unit, concept, type, etc.)
 * - Progress-based practice (questions from what student studied)
 * - Weak area detection and targeted practice
 * - Practice session management
 */
@Singleton
class QuizBankRepository @Inject constructor(
    private val questionDao: QuestionDao,
    private val questionStatsDao: QuestionStatsDao,
    private val questionConceptDao: QuestionConceptDao,
    private val examDao: ExamDao,
    private val examQuestionDao: ExamQuestionDao,
    private val practiceSessionDao: PracticeSessionDao,
    private val progressRepository: ProgressRepository,  // NEW - replaced ProgressDao
    private val conceptDao: ConceptDao,
    private val lessonDao: LessonDao,
    private val unitDao: UnitDao
) {

    // ==================== Exam Queries ====================

    fun getAllExams(): Flow<List<Exam>> = examDao.getAllExams()

    fun getExamsBySubject(subjectId: String): Flow<List<Exam>> =
        examDao.getExamsBySubject(subjectId)

    fun getExamsBySource(source: ExamSource): Flow<List<Exam>> =
        examDao.getExamsBySource(source)

    suspend fun getExamById(examId: String): Exam? =
        examDao.getExamById(examId)

    suspend fun getQuestionsForExam(examId: String): List<Question> =
        questionDao.getQuestionsForExam(examId)

    fun getQuestionsForExamFlow(examId: String): Flow<List<Question>> =
        questionDao.getQuestionsForExamFlow(examId)

    // ==================== Question Queries ====================

    suspend fun getQuestionById(questionId: String): Question? =
        questionDao.getQuestionById(questionId)

    fun getQuestionsBySubject(subjectId: String): Flow<List<Question>> =
        questionDao.getQuestionsBySubject(subjectId)

    fun getQuestionsByUnit(unitId: String): Flow<List<Question>> =
        questionDao.getQuestionsByUnit(unitId)

    fun getQuestionsByType(subjectId: String, type: QuestionType): Flow<List<Question>> =
        questionDao.getQuestionsByType(subjectId, type)

    fun getFeedEligibleQuestions(subjectId: String): Flow<List<Question>> =
        questionDao.getFeedEligibleQuestions(subjectId)

    /**
     * Get questions for feed, excluding recently shown ones.
     */
    suspend fun getQuestionsForFeed(
        subjectId: String,
        conceptIds: List<String>,
        limit: Int = 20,
        excludeShownWithinHours: Int = 24
    ): List<Question> {
        val threshold = System.currentTimeMillis() - (excludeShownWithinHours * 60 * 60 * 1000L)
        val notRecentlyShown = questionStatsDao.getQuestionsNotRecentlyShown(threshold)
            .map { it.questionId }
            .toSet()

        return questionDao.getQuestionsByConceptIds(subjectId, conceptIds)
            .first()
            .filter { it.feedEligible && it.id in notRecentlyShown }
            .shuffled()
            .take(limit)
    }

    // ==================== Progress-Based Queries ====================

    /**
     * Get questions based on what the student has studied.
     * Flexible: includes questions from completed units AND current unit.
     */
    suspend fun getQuestionsByProgress(
        subjectId: String,
        limit: Int = 20,
        includeTypes: List<QuestionType>? = null,
        difficulty: IntRange? = null
    ): List<Question> {
        // Get completed lessons
        val completedLessons = progressRepository.getCompletedLessons().first()
        val completedLessonIds = completedLessons.map { it.lessonId }.toSet()

        // Get lessons to find their units
        val lessons = lessonDao.getLessonsBySubject(subjectId).first()
        val completedUnitIds = lessons
            .filter { it.id in completedLessonIds }
            .map { it.unitId }
            .toSet()

        // Get current unit (unit with at least one incomplete lesson that was accessed)
        val recentLessons = progressRepository.getRecentlyAccessedLessons(5).first()
        val currentUnitIds = lessons
            .filter { lesson ->
                recentLessons.any { it.lessonId == lesson.id && !it.completed }
            }
            .map { it.unitId }
            .toSet()

        val targetUnitIds = (completedUnitIds + currentUnitIds).toList()

        if (targetUnitIds.isEmpty()) {
            return emptyList()
        }

        // Get questions from these units
        var questions = questionDao.getQuestionsByUnitIds(subjectId, targetUnitIds).first()

        // Apply filters
        if (includeTypes != null) {
            questions = questions.filter { it.type in includeTypes }
        }

        if (difficulty != null) {
            questions = questions.filter { it.difficulty in difficulty }
        }

        // Shuffle and apply dynamic difficulty distribution
        return applyDifficultyDistribution(questions, limit)
    }

    /**
     * Apply dynamic difficulty distribution to avoid boredom.
     * Mix of easy, medium, hard questions with some randomization.
     */
    private fun applyDifficultyDistribution(
        questions: List<Question>,
        limit: Int
    ): List<Question> {
        if (questions.size <= limit) {
            return questions.shuffled()
        }

        // Group by difficulty
        val easy = questions.filter { it.difficulty <= 2 }.shuffled()
        val medium = questions.filter { it.difficulty == 3 }.shuffled()
        val hard = questions.filter { it.difficulty >= 4 }.shuffled()

        // Target distribution: 30% easy, 50% medium, 20% hard (with variance)
        val easyCount = (limit * 0.3).toInt().coerceAtMost(easy.size)
        val hardCount = (limit * 0.2).toInt().coerceAtMost(hard.size)
        val mediumCount = (limit - easyCount - hardCount).coerceAtMost(medium.size)

        val result = mutableListOf<Question>()
        result.addAll(easy.take(easyCount))
        result.addAll(medium.take(mediumCount))
        result.addAll(hard.take(hardCount))

        // If we don't have enough, fill with whatever's available
        if (result.size < limit) {
            val remaining = questions.filter { it !in result }.shuffled()
            result.addAll(remaining.take(limit - result.size))
        }

        // Shuffle the final result so it's not easy->medium->hard
        return result.shuffled()
    }

    // ==================== Weak Area Detection ====================

    /**
     * Get concepts where the student is struggling.
     * Based on question response history.
     */
    suspend fun getWeakConcepts(
        subjectId: String,
        minAttempts: Int = 3,
        maxSuccessRate: Float = 0.5f
    ): List<String> {
        val stats = questionStatsDao.getHardestQuestions(50).first()
            .filter { it.timesAsked >= minAttempts && it.successRate <= maxSuccessRate }
            .map { it.questionId }

        if (stats.isEmpty()) return emptyList()

        // Get concepts for these questions
        return questionConceptDao.getConceptsForQuestions(stats)
            .groupBy { it }
            .entries
            .sortedByDescending { it.value.size }
            .map { it.key }
            .take(10)
    }

    /**
     * Get questions targeting weak areas.
     */
    suspend fun getWeakAreaQuestions(
        subjectId: String,
        limit: Int = 20
    ): List<Question> {
        val weakConcepts = getWeakConcepts(subjectId)

        if (weakConcepts.isEmpty()) {
            // No weak areas detected, return random questions
            return getQuestionsByProgress(subjectId, limit)
        }

        val questions = questionDao.getQuestionsByConceptIds(subjectId, weakConcepts).first()

        // Start with easier questions to build confidence
        return questions
            .sortedBy { it.difficulty }
            .take(limit)
    }

    // ==================== Practice Session Management ====================

    suspend fun getActiveSession(): PracticeSession? =
        practiceSessionDao.getActiveSession()

    fun getRecentSessions(limit: Int = 10): Flow<List<PracticeSession>> =
        practiceSessionDao.getRecentCompletedSessions(limit)

    fun getSessionsBySubject(subjectId: String): Flow<List<PracticeSession>> =
        practiceSessionDao.getSessionsBySubject(subjectId)

    suspend fun getSession(sessionId: Long): PracticeSession? =
        practiceSessionDao.getSession(sessionId)

    suspend fun getSessionQuestions(sessionId: Long): List<PracticeQuestion> =
        practiceSessionDao.getQuestionsForSession(sessionId)

    /**
     * Create a new practice session based on generation type.
     */
    suspend fun createPracticeSession(
        subjectId: String,
        generationType: PracticeGenerationType,
        questionCount: Int = 20,
        filterUnitIds: List<String>? = null,
        filterConceptIds: List<String>? = null,
        filterQuestionTypes: List<QuestionType>? = null,
        filterDifficulty: IntRange? = null
    ): Long {
        // Get questions based on generation type
        val questions = when (generationType) {
            PracticeGenerationType.BY_UNIT -> {
                filterUnitIds?.let { units ->
                    questionDao.getQuestionsByUnitIds(subjectId, units).first()
                } ?: emptyList()
            }
            PracticeGenerationType.BY_CONCEPT -> {
                filterConceptIds?.let { concepts ->
                    questionDao.getQuestionsByConceptIds(subjectId, concepts).first()
                } ?: emptyList()
            }
            PracticeGenerationType.BY_PROGRESS -> {
                getQuestionsByProgress(subjectId, questionCount, filterQuestionTypes, filterDifficulty)
            }
            PracticeGenerationType.WEAK_AREAS -> {
                getWeakAreaQuestions(subjectId, questionCount)
            }
            PracticeGenerationType.QUICK_REVIEW -> {
                questionDao.getQuestionsBySubject(subjectId).first()
                    .filter { filterQuestionTypes == null || it.type in filterQuestionTypes }
                    .shuffled()
                    .take(10)
            }
            PracticeGenerationType.BY_TYPE -> {
                filterQuestionTypes?.flatMap { type ->
                    questionDao.getQuestionsByType(subjectId, type).first()
                }?.shuffled() ?: emptyList()
            }
            else -> {
                getQuestionsByProgress(subjectId, questionCount)
            }
        }

        // Apply additional filters
        var filteredQuestions = questions

        if (filterQuestionTypes != null && generationType != PracticeGenerationType.BY_TYPE) {
            filteredQuestions = filteredQuestions.filter { it.type in filterQuestionTypes }
        }

        if (filterDifficulty != null && generationType != PracticeGenerationType.BY_PROGRESS) {
            filteredQuestions = filteredQuestions.filter { it.difficulty in filterDifficulty }
        }

        // Apply difficulty distribution and limit
        val finalQuestions = applyDifficultyDistribution(filteredQuestions, questionCount)

        // Create session
        val session = PracticeSession(
            subjectId = subjectId,
            generationType = generationType,
            filterUnitIds = filterUnitIds?.joinToString(","),
            filterConceptIds = filterConceptIds?.joinToString(","),
            filterQuestionTypes = filterQuestionTypes?.joinToString(",") { it.name },
            questionCount = finalQuestions.size
        )

        return practiceSessionDao.createSessionWithQuestions(
            session,
            finalQuestions.map { it.id }
        )
    }

    /**
     * Record an answer for a practice session question.
     */
    suspend fun recordPracticeAnswer(
        sessionId: Long,
        questionId: String,
        answer: String,
        isCorrect: Boolean,
        timeSeconds: Int
    ) {
        // Update practice question
        practiceSessionDao.recordAnswer(sessionId, questionId, answer, isCorrect, timeSeconds)

        // Update session counts
        if (isCorrect) {
            practiceSessionDao.incrementCorrectCount(sessionId)
        } else {
            practiceSessionDao.incrementWrongCount(sessionId)
        }

        // Update question stats
        questionStatsDao.recordResponse(questionId, isCorrect, timeSeconds)

        // Update current question index
        val session = practiceSessionDao.getSession(sessionId) ?: return
        practiceSessionDao.updateCurrentQuestion(sessionId, session.currentQuestionIndex + 1)
    }

    /**
     * Complete a practice session.
     */
    suspend fun completePracticeSession(sessionId: Long) {
        practiceSessionDao.completeSession(sessionId)
    }

    /**
     * Skip a question in a practice session.
     */
    suspend fun skipQuestion(sessionId: Long, questionId: String) {
        practiceSessionDao.markQuestionSkipped(sessionId, questionId)
        practiceSessionDao.incrementSkippedCount(sessionId)

        val session = practiceSessionDao.getSession(sessionId) ?: return
        practiceSessionDao.updateCurrentQuestion(sessionId, session.currentQuestionIndex + 1)
    }

    // ==================== Question Stats ====================

    suspend fun recordQuestionShownInFeed(questionId: String) {
        questionStatsDao.recordFeedShow(questionId)
    }

    suspend fun recordQuestionShownInFeedBatch(questionIds: List<String>) {
        questionStatsDao.recordFeedShowBatch(questionIds)
    }

    fun getQuestionStats(questionId: String): Flow<QuestionStats?> =
        questionStatsDao.getStatsForQuestionFlow(questionId)

    // ==================== Aggregates ====================

    fun getAverageScoreForSubject(subjectId: String): Flow<Float?> =
        practiceSessionDao.getAverageScore(subjectId)

    fun getCompletedSessionCount(subjectId: String): Flow<Int> =
        practiceSessionDao.getCompletedSessionCount(subjectId)

    /**
     * Get total question count by various criteria.
     */
    suspend fun getQuestionCounts(subjectId: String): QuestionCounts {
        val all = questionDao.getQuestionsBySubject(subjectId).first()

        return QuestionCounts(
            total = all.size,
            byType = all.groupBy { it.type }.mapValues { it.value.size },
            bySource = all.groupBy { it.source }.mapValues { it.value.size },
            byDifficulty = all.groupBy { it.difficulty }.mapValues { it.value.size },
            feedEligible = all.count { it.feedEligible }
        )
    }
}

/**
 * Summary of question counts for display.
 */
data class QuestionCounts(
    val total: Int,
    val byType: Map<QuestionType, Int>,
    val bySource: Map<QuestionSource, Int>,
    val byDifficulty: Map<Int, Int>,
    val feedEligible: Int
)