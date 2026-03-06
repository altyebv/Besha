package com.zeros.basheer.feature.quizbank.domain.repository

import com.zeros.basheer.feature.quizbank.data.dao.ExamDao
import com.zeros.basheer.feature.quizbank.data.dao.ExamQuestionDao
import com.zeros.basheer.feature.quizbank.data.dao.QuestionConceptDao
import com.zeros.basheer.feature.quizbank.data.dao.QuestionDao
import com.zeros.basheer.feature.quizbank.data.dao.QuestionResponseDao
import com.zeros.basheer.feature.quizbank.data.dao.QuestionStatsDao
import com.zeros.basheer.feature.quizbank.data.dao.QuizAttemptDao
import com.zeros.basheer.feature.quizbank.data.mapper.toDomain
import com.zeros.basheer.feature.quizbank.data.mapper.toDomainList
import com.zeros.basheer.feature.quizbank.data.mapper.toEntity
import com.zeros.basheer.feature.quizbank.domain.model.Exam
import com.zeros.basheer.feature.quizbank.domain.model.ExamAttemptStatus
import com.zeros.basheer.feature.quizbank.domain.model.ExamQuestion
import com.zeros.basheer.feature.quizbank.domain.model.ExamSource
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.model.QuestionConcept
import com.zeros.basheer.feature.quizbank.domain.model.QuestionCounts
import com.zeros.basheer.feature.quizbank.domain.model.QuestionResponse
import com.zeros.basheer.feature.quizbank.domain.model.QuestionStats
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import com.zeros.basheer.feature.quizbank.domain.model.QuizAttempt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizBankRepositoryImpl @Inject constructor(
    private val questionDao: QuestionDao,
    private val examDao: ExamDao,
    private val examQuestionDao: ExamQuestionDao,
    private val questionConceptDao: QuestionConceptDao,
    private val quizAttemptDao: QuizAttemptDao,
    private val questionResponseDao: QuestionResponseDao,
    private val questionStatsDao: QuestionStatsDao
) : QuizBankRepository {

    // ==================== Questions ====================

    override suspend fun getQuestionById(questionId: String): Question? =
        questionDao.getQuestionById(questionId)?.toDomain()

    override suspend fun getCheckpointsForLesson(lessonId: String): Map<String, Question> =
        questionDao.getCheckpointsForLesson(lessonId)
            .filter { it.sectionId != null }
            .associate { entity -> entity.sectionId!! to entity.toDomain() }

    override suspend fun getCheckpointsForPart(lessonId: String, partIndex: Int): Map<String, Question> =
        questionDao.getCheckpointsForPart(lessonId, partIndex)
            .filter { it.sectionId != null }
            .associate { entity -> entity.sectionId!! to entity.toDomain() }


    override fun getQuestionsBySubject(subjectId: String): Flow<List<Question>> =
        questionDao.getQuestionsBySubject(subjectId).map { it.map { e -> e.toDomain() } }

    override fun getQuestionsByUnit(unitId: String): Flow<List<Question>> =
        questionDao.getQuestionsByUnit(unitId).map { it.map { e -> e.toDomain() } }

    override fun getQuestionsByLesson(lessonId: String): Flow<List<Question>> =
        questionDao.getQuestionsByLesson(lessonId).map { it.map { e -> e.toDomain() } }

    override fun getQuestionsByType(subjectId: String, type: QuestionType): Flow<List<Question>> =
        questionDao.getQuestionsBySubjectAndType(subjectId, type.name).map { it.map { e -> e.toDomain() } }

    override fun getFeedEligibleQuestions(subjectId: String): Flow<List<Question>> =
        questionDao.getFeedEligibleQuestions(subjectId).map { it.map { e -> e.toDomain() } }

    override fun getQuestionsByConcept(conceptId: String): Flow<List<Question>> =
        questionDao.getQuestionsByConcept(conceptId).map { it.map { e -> e.toDomain() } }

    override suspend fun getQuestionsForConcepts(conceptIds: List<String>, limit: Int): List<Question> =
        questionDao.getQuestionsForConcepts(conceptIds, limit).map { it.toDomain() }

    override suspend fun getFilteredQuestions(
        subjectId: String,
        unitId: String?,
        type: QuestionType?,
        conceptId: String?,
        minDifficulty: Int?,
        maxDifficulty: Int?,
        limit: Int
    ): List<Question> =
        questionDao.getFilteredQuestions(
            subjectId = subjectId,
            unitId = unitId,
            type = type?.name,
            conceptId = conceptId,
            minDifficulty = minDifficulty,
            maxDifficulty = maxDifficulty,
            limit = limit
        ).map { it.toDomain() }

    override suspend fun insertQuestion(question: Question) =
        questionDao.insertQuestion(question.toEntity())

    override suspend fun insertQuestions(questions: List<Question>) =
        questionDao.insertQuestions(questions.map { it.toEntity() })

    override suspend fun deleteQuestionById(questionId: String) =
        questionDao.deleteQuestionById(questionId)

    override suspend fun getQuestionCounts(subjectId: String): QuestionCounts {
        val all = questionDao.getQuestionsBySubject(subjectId).toDomainList().first()
        return QuestionCounts(
            total = all.size,
            byType = all.groupBy { it.type }.mapValues { it.value.size },
            bySource = all.groupBy { it.source }.mapValues { it.value.size },
            byDifficulty = all.groupBy { it.difficulty }.mapValues { it.value.size },
            feedEligible = all.count { it.feedEligible }
        )
    }

    // ==================== Exams ====================

    override fun getAllExams(): Flow<List<Exam>> =
        examDao.getAllExams().map { it.map { e -> e.toDomain() } }

    override fun getExamsBySubject(subjectId: String): Flow<List<Exam>> =
        examDao.getExamsBySubject(subjectId).map { it.map { e -> e.toDomain() } }

    override fun getExamsBySource(subjectId: String, source: ExamSource): Flow<List<Exam>> =
        examDao.getExamsBySubjectAndSource(subjectId, source.name).map { it.map { e -> e.toDomain() } }

    override suspend fun getExamById(examId: String): Exam? =
        examDao.getExamById(examId)?.toDomain()

    override suspend fun getQuestionsForExam(examId: String): List<Question> =
        questionDao.getQuestionsForExam(examId).map { it.toDomain() }

    override fun getQuestionsForExamFlow(examId: String): Flow<List<Question>> =
        questionDao.getQuestionsForExamFlow(examId).map { it.map { e -> e.toDomain() } }

    override suspend fun insertExam(exam: Exam) =
        examDao.insertExam(exam.toEntity())

    override suspend fun insertExams(exams: List<Exam>) =
        examDao.insertExams(exams.map { it.toEntity() })

    override suspend fun deleteExamsBySubject(subjectId: String) =
        examDao.deleteExamsBySubject(subjectId)

    // ==================== ExamQuestions Junction ====================

    override suspend fun insertExamQuestion(examQuestion: ExamQuestion) =
        examQuestionDao.insert(examQuestion.toEntity())

    override suspend fun insertExamQuestions(examQuestions: List<ExamQuestion>) =
        examQuestionDao.insertAll(examQuestions.map { it.toEntity() })

    override suspend fun deleteExamQuestionsByExam(examId: String) =
        examQuestionDao.deleteByExamId(examId)

    // ==================== QuestionConcepts Junction ====================

    override suspend fun insertQuestionConcept(questionConcept: QuestionConcept) =
        questionConceptDao.insert(questionConcept.toEntity())

    override suspend fun insertQuestionConcepts(questionConcepts: List<QuestionConcept>) =
        questionConceptDao.insertAll(questionConcepts.map { it.toEntity() })

    override suspend fun deleteQuestionConceptsByQuestion(questionId: String) =
        questionConceptDao.deleteByQuestionId(questionId)

    // ==================== Quiz Attempts ====================

    override fun getAttemptsByExam(examId: String): Flow<List<QuizAttempt>> =
        quizAttemptDao.getAttemptsByExam(examId).map { it.map { e -> e.toDomain() } }

    override suspend fun getAttemptById(attemptId: Long): QuizAttempt? =
        quizAttemptDao.getAttemptById(attemptId)?.toDomain()

    override fun getRecentAttempts(limit: Int): Flow<List<QuizAttempt>> =
        quizAttemptDao.getRecentAttempts(limit).map { it.map { e -> e.toDomain() } }

    override suspend fun getLastAttemptForExam(examId: String): QuizAttempt? =
        quizAttemptDao.getLastAttemptForExam(examId)?.toDomain()

    override suspend fun insertAttempt(attempt: QuizAttempt): Long =
        quizAttemptDao.insertAttempt(attempt.toEntity())

    override suspend fun updateAttempt(attempt: QuizAttempt) =
        quizAttemptDao.updateAttempt(attempt.toEntity())

    override suspend fun completeAttempt(attemptId: Long, score: Int, totalPoints: Int, timeSpentSeconds: Int) =
        quizAttemptDao.completeAttempt(attemptId, score, totalPoints, timeSpentSeconds)

    override suspend fun deleteAttemptsByExam(examId: String) =
        quizAttemptDao.deleteAttemptsByExam(examId)

    // ==================== Question Responses ====================

    override fun getResponsesByAttempt(attemptId: Long): Flow<List<QuestionResponse>> =
        questionResponseDao.getResponsesByAttempt(attemptId).map { it.map { e -> e.toDomain() } }

    override suspend fun getResponse(attemptId: Long, questionId: String): QuestionResponse? =
        questionResponseDao.getResponse(attemptId, questionId)?.toDomain()

    override suspend fun insertResponse(response: QuestionResponse): Long =
        questionResponseDao.insertResponse(response.toEntity())

    override suspend fun insertResponses(responses: List<QuestionResponse>) =
        questionResponseDao.insertResponses(responses.map { it.toEntity() })

    // ==================== Question Stats ====================

    override suspend fun getStatsForQuestion(questionId: String): QuestionStats? =
        questionStatsDao.getStatsForQuestion(questionId)?.toDomain()

    override fun getStatsForQuestionFlow(questionId: String): Flow<QuestionStats?> =
        questionStatsDao.getStatsForQuestionFlow(questionId).map { it?.toDomain() }

    override suspend fun getStatsForQuestions(questionIds: List<String>): List<QuestionStats> =
        questionStatsDao.getStatsForQuestions(questionIds).map { it.toDomain() }

    override fun getHardestQuestions(limit: Int): Flow<List<QuestionStats>> =
        questionStatsDao.getHardestQuestions(limit).map { it.map { e -> e.toDomain() } }

    override fun getEasiestQuestions(limit: Int): Flow<List<QuestionStats>> =
        questionStatsDao.getEasiestQuestions(limit).map { it.map { e -> e.toDomain() } }


    override suspend fun getWeakQuestionsForSubject(
        subjectId: String,
        minAttempts: Int,
        threshold: Float,
        limit: Int
    ): List<QuestionStats> =
        questionStatsDao.getWeakQuestionsForSubject(
            subjectId = subjectId,
            minAttempts = minAttempts,
            threshold = threshold,
            limit = limit
        ).map { it.toDomain() }

    override suspend fun insertStats(stats: QuestionStats) =
        questionStatsDao.insertStats(stats.toEntity())

    override suspend fun updateStats(stats: QuestionStats) =
        questionStatsDao.updateStats(stats.toEntity())

    // ==================== Convenience Methods ====================

    override suspend fun startQuizAttempt(examId: String): Long {
        val attempt = QuizAttempt(
            id = 0,
            examId = examId,
            startedAt = System.currentTimeMillis(),
            completedAt = null,
            score = null,
            totalPoints = null,
            percentage = null,
            timeSpentSeconds = null
        )
        return insertAttempt(attempt)
    }

    override suspend fun completeQuizAttempt(
        attemptId: Long,
        score: Int,
        totalPoints: Int,
        timeSpentSeconds: Int,
        status: String
    ) {
        quizAttemptDao.completeAttempt(attemptId, score, totalPoints, timeSpentSeconds, status)
    }

    override suspend fun recordQuestionResponse(
        attemptId: Long,
        questionId: String,
        userAnswer: String,
        isCorrect: Boolean,
        pointsEarned: Int,
        timeSpentSeconds: Int
    ) {
        val response = QuestionResponse(
            id = 0,
            attemptId = attemptId,
            questionId = questionId,
            userAnswer = userAnswer,
            isCorrect = isCorrect,
            pointsEarned = pointsEarned,
            timeSpentSeconds = timeSpentSeconds,
            answeredAt = System.currentTimeMillis()
        )
        insertResponse(response)

        val currentStats = getStatsForQuestion(questionId)
        val updatedStats = if (currentStats != null) {
            currentStats.withNewResponse(isCorrect, timeSpentSeconds)
        } else {
            QuestionStats.forNewQuestion(questionId).withNewResponse(isCorrect, timeSpentSeconds)
        }

        if (currentStats != null) updateStats(updatedStats) else insertStats(updatedStats)
    }
}