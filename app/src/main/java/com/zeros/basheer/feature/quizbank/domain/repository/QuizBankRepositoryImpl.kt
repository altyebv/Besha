package com.zeros.basheer.feature.quizbank.domain.repository


import com.zeros.basheer.feature.quizbank.data.dao.*
import com.zeros.basheer.feature.quizbank.data.entity.*
import com.zeros.basheer.feature.quizbank.data.mapper.toDomainList
import com.zeros.basheer.feature.quizbank.domain.model.*
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

    // ==================== Mappers ====================

    private fun questionEntityToDomain(entity: QuestionEntity): Question = Question(
        id = entity.id,
        subjectId = entity.subjectId,
        unitId = entity.unitId,
        lessonId = entity.lessonId,
        type = QuestionType.valueOf(entity.type),
        textAr = entity.textAr,
        textEn = entity.textEn,
        correctAnswer = entity.correctAnswer,
        options = entity.options,
        explanation = entity.explanation,
        imageUrl = entity.imageUrl,
        tableData = entity.tableData,
        source = QuestionSource.valueOf(entity.source),
        sourceExamId = entity.sourceExamId,
        sourceDetails = entity.sourceDetails,
        sourceYear = entity.sourceYear,
        difficulty = entity.difficulty,
        cognitiveLevel = CognitiveLevel.valueOf(entity.cognitiveLevel),
        points = entity.points,
        estimatedSeconds = entity.estimatedSeconds,
        feedEligible = entity.feedEligible,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    private fun questionDomainToEntity(question: Question): QuestionEntity = QuestionEntity(
        id = question.id,
        subjectId = question.subjectId,
        unitId = question.unitId,
        lessonId = question.lessonId,
        type = question.type.name,
        textAr = question.textAr,
        textEn = question.textEn,
        correctAnswer = question.correctAnswer,
        options = question.options,
        explanation = question.explanation,
        imageUrl = question.imageUrl,
        tableData = question.tableData,
        source = question.source.name,
        sourceExamId = question.sourceExamId,
        sourceDetails = question.sourceDetails,
        sourceYear = question.sourceYear,
        difficulty = question.difficulty,
        cognitiveLevel = question.cognitiveLevel.name,
        points = question.points,
        estimatedSeconds = question.estimatedSeconds,
        feedEligible = question.feedEligible,
        createdAt = question.createdAt,
        updatedAt = question.updatedAt
    )

    private fun examEntityToDomain(entity: ExamEntity): Exam = Exam(
        id = entity.id,
        subjectId = entity.subjectId,
        titleAr = entity.titleAr,
        titleEn = entity.titleEn,
        source = ExamSource.valueOf(entity.source),
        year = entity.year,
        schoolName = entity.schoolName,
        duration = entity.duration,
        totalPoints = entity.totalPoints,
        description = entity.description
    )

    private fun examDomainToEntity(exam: Exam): ExamEntity = ExamEntity(
        id = exam.id,
        subjectId = exam.subjectId,
        titleAr = exam.titleAr,
        titleEn = exam.titleEn,
        source = exam.source.name,
        year = exam.year,
        schoolName = exam.schoolName,
        duration = exam.duration,
        totalPoints = exam.totalPoints,
        description = exam.description
    )

    private fun examQuestionEntityToDomain(entity: ExamQuestionEntity): ExamQuestion = ExamQuestion(
        examId = entity.examId,
        questionId = entity.questionId,
        order = entity.order,
        sectionLabel = entity.sectionLabel,
        points = entity.points
    )

    private fun examQuestionDomainToEntity(examQuestion: ExamQuestion): ExamQuestionEntity = ExamQuestionEntity(
        examId = examQuestion.examId,
        questionId = examQuestion.questionId,
        order = examQuestion.order,
        sectionLabel = examQuestion.sectionLabel,
        points = examQuestion.points
    )

    private fun questionConceptEntityToDomain(entity: QuestionConceptEntity): QuestionConcept = QuestionConcept(
        questionId = entity.questionId,
        conceptId = entity.conceptId,
        isPrimary = entity.isPrimary
    )

    private fun questionConceptDomainToEntity(questionConcept: QuestionConcept): QuestionConceptEntity = QuestionConceptEntity(
        questionId = questionConcept.questionId,
        conceptId = questionConcept.conceptId,
        isPrimary = questionConcept.isPrimary
    )

    private fun quizAttemptEntityToDomain(entity: QuizAttemptEntity): QuizAttempt = QuizAttempt(
        id = entity.id,
        examId = entity.examId,
        startedAt = entity.startedAt,
        completedAt = entity.completedAt,
        score = entity.score,
        totalPoints = entity.totalPoints,
        percentage = entity.percentage,
        timeSpentSeconds = entity.timeSpentSeconds
    )

    private fun quizAttemptDomainToEntity(attempt: QuizAttempt): QuizAttemptEntity = QuizAttemptEntity(
        id = attempt.id,
        examId = attempt.examId,
        startedAt = attempt.startedAt,
        completedAt = attempt.completedAt,
        score = attempt.score,
        totalPoints = attempt.totalPoints,
        percentage = attempt.percentage,
        timeSpentSeconds = attempt.timeSpentSeconds
    )

    private fun questionResponseEntityToDomain(entity: QuestionResponseEntity): QuestionResponse = QuestionResponse(
        id = entity.id,
        attemptId = entity.attemptId,
        questionId = entity.questionId,
        userAnswer = entity.userAnswer,
        isCorrect = entity.isCorrect,
        pointsEarned = entity.pointsEarned,
        timeSpentSeconds = entity.timeSpentSeconds,
        answeredAt = entity.answeredAt
    )

    private fun questionResponseDomainToEntity(response: QuestionResponse): QuestionResponseEntity = QuestionResponseEntity(
        id = response.id,
        attemptId = response.attemptId,
        questionId = response.questionId,
        userAnswer = response.userAnswer,
        isCorrect = response.isCorrect,
        pointsEarned = response.pointsEarned,
        timeSpentSeconds = response.timeSpentSeconds,
        answeredAt = response.answeredAt
    )

    private fun questionStatsEntityToDomain(entity: QuestionStatsEntity): QuestionStats = QuestionStats(
        questionId = entity.questionId,
        timesAsked = entity.timesAsked,
        timesCorrect = entity.timesCorrect,
        avgTimeSeconds = entity.avgTimeSeconds,
        successRate = entity.successRate,
        lastShownInFeed = entity.lastShownInFeed,
        feedShowCount = entity.feedShowCount,
        lastAskedAt = entity.lastAskedAt,
        updatedAt = entity.updatedAt
    )

    private fun questionStatsDomainToEntity(stats: QuestionStats): QuestionStatsEntity = QuestionStatsEntity(
        questionId = stats.questionId,
        timesAsked = stats.timesAsked,
        timesCorrect = stats.timesCorrect,
        avgTimeSeconds = stats.avgTimeSeconds,
        successRate = stats.successRate,
        lastShownInFeed = stats.lastShownInFeed,
        feedShowCount = stats.feedShowCount,
        lastAskedAt = stats.lastAskedAt,
        updatedAt = stats.updatedAt
    )

    // ==================== Questions ====================

    override suspend fun getQuestionById(questionId: String): Question? =
        questionDao.getQuestionById(questionId)?.let { questionEntityToDomain(it) }

    override fun getQuestionsBySubject(subjectId: String): Flow<List<Question>> =
        questionDao.getQuestionsBySubject(subjectId).map { entities ->
            entities.map { questionEntityToDomain(it) }
        }

    override fun getQuestionsByUnit(unitId: String): Flow<List<Question>> =
        questionDao.getQuestionsByUnit(unitId).map { entities ->
            entities.map { questionEntityToDomain(it) }
        }

    override fun getQuestionsByLesson(lessonId: String): Flow<List<Question>> =
        questionDao.getQuestionsByLesson(lessonId).map { entities ->
            entities.map { questionEntityToDomain(it) }
        }

    override fun getQuestionsByType(subjectId: String, type: QuestionType): Flow<List<Question>> =
        questionDao.getQuestionsBySubjectAndType(subjectId, type.name).map { entities ->
            entities.map { questionEntityToDomain(it) }
        }

    override fun getFeedEligibleQuestions(subjectId: String): Flow<List<Question>> =
        questionDao.getFeedEligibleQuestions(subjectId).map { entities ->
            entities.map { questionEntityToDomain(it) }
        }

    override fun getQuestionsByConcept(conceptId: String): Flow<List<Question>> =
        questionDao.getQuestionsByConcept(conceptId).map { entities ->
            entities.map { questionEntityToDomain(it) }
        }

    override suspend fun getQuestionsForConcepts(conceptIds: List<String>, limit: Int): List<Question> =
        questionDao.getQuestionsForConcepts(conceptIds, limit).map { questionEntityToDomain(it) }

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
        ).map { questionEntityToDomain(it) }

    override suspend fun insertQuestion(question: Question) =
        questionDao.insertQuestion(questionDomainToEntity(question))

    override suspend fun insertQuestions(questions: List<Question>) =
        questionDao.insertQuestions(questions.map { questionDomainToEntity(it) })

    override suspend fun deleteQuestionById(questionId: String) =
        questionDao.deleteQuestionById(questionId)

    override suspend fun getQuestionCounts(subjectId: String): QuestionCounts {
        val all = questionDao
            .getQuestionsBySubject(subjectId)
            .toDomainList()   // 👈 THIS is what fixes the original error
            .first()

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
        examDao.getAllExams().map { entities ->
            entities.map { examEntityToDomain(it) }
        }

    override fun getExamsBySubject(subjectId: String): Flow<List<Exam>> =
        examDao.getExamsBySubject(subjectId).map { entities ->
            entities.map { examEntityToDomain(it) }
        }

    override fun getExamsBySource(subjectId: String, source: ExamSource): Flow<List<Exam>> =
        examDao.getExamsBySubjectAndSource(subjectId, source.name).map { entities ->
            entities.map { examEntityToDomain(it) }
        }

    override suspend fun getExamById(examId: String): Exam? =
        examDao.getExamById(examId)?.let { examEntityToDomain(it) }

    override suspend fun getQuestionsForExam(examId: String): List<Question> =
        questionDao.getQuestionsForExam(examId).map { questionEntityToDomain(it) }

    override fun getQuestionsForExamFlow(examId: String): Flow<List<Question>> =
        questionDao.getQuestionsForExamFlow(examId).map { entities ->
            entities.map { questionEntityToDomain(it) }
        }

    override suspend fun insertExam(exam: Exam) =
        examDao.insertExam(examDomainToEntity(exam))

    override suspend fun insertExams(exams: List<Exam>) =
        examDao.insertExams(exams.map { examDomainToEntity(it) })

    override suspend fun deleteExamsBySubject(subjectId: String) =
        examDao.deleteExamsBySubject(subjectId)

    // ==================== ExamQuestions Junction ====================

    override suspend fun insertExamQuestion(examQuestion: ExamQuestion) =
        examQuestionDao.insert(examQuestionDomainToEntity(examQuestion))

    override suspend fun insertExamQuestions(examQuestions: List<ExamQuestion>) =
        examQuestionDao.insertAll(examQuestions.map { examQuestionDomainToEntity(it) })

    override suspend fun deleteExamQuestionsByExam(examId: String) =
        examQuestionDao.deleteByExamId(examId)

    // ==================== QuestionConcepts Junction ====================

    override suspend fun insertQuestionConcept(questionConcept: QuestionConcept) =
        questionConceptDao.insert(questionConceptDomainToEntity(questionConcept))

    override suspend fun insertQuestionConcepts(questionConcepts: List<QuestionConcept>) =
        questionConceptDao.insertAll(questionConcepts.map { questionConceptDomainToEntity(it) })

    override suspend fun deleteQuestionConceptsByQuestion(questionId: String) =
        questionConceptDao.deleteByQuestionId(questionId)

    // ==================== Quiz Attempts ====================

    override fun getAttemptsByExam(examId: String): Flow<List<QuizAttempt>> =
        quizAttemptDao.getAttemptsByExam(examId).map { entities ->
            entities.map { quizAttemptEntityToDomain(it) }
        }

    override suspend fun getAttemptById(attemptId: Long): QuizAttempt? =
        quizAttemptDao.getAttemptById(attemptId)?.let { quizAttemptEntityToDomain(it) }

    override fun getRecentAttempts(limit: Int): Flow<List<QuizAttempt>> =
        quizAttemptDao.getRecentAttempts(limit).map { entities ->
            entities.map { quizAttemptEntityToDomain(it) }
        }

    override suspend fun getLastAttemptForExam(examId: String): QuizAttempt? =
        quizAttemptDao.getLastAttemptForExam(examId)?.let { quizAttemptEntityToDomain(it) }

    override suspend fun insertAttempt(attempt: QuizAttempt): Long =
        quizAttemptDao.insertAttempt(quizAttemptDomainToEntity(attempt))

    override suspend fun updateAttempt(attempt: QuizAttempt) =
        quizAttemptDao.updateAttempt(quizAttemptDomainToEntity(attempt))

    override suspend fun completeAttempt(attemptId: Long, score: Int, totalPoints: Int, timeSpentSeconds: Int) =
        quizAttemptDao.completeAttempt(attemptId, score, totalPoints, timeSpentSeconds)

    override suspend fun deleteAttemptsByExam(examId: String) =
        quizAttemptDao.deleteAttemptsByExam(examId)

    // ==================== Question Responses ====================

    override fun getResponsesByAttempt(attemptId: Long): Flow<List<QuestionResponse>> =
        questionResponseDao.getResponsesByAttempt(attemptId).map { entities ->
            entities.map { questionResponseEntityToDomain(it) }
        }

    override suspend fun getResponse(attemptId: Long, questionId: String): QuestionResponse? =
        questionResponseDao.getResponse(attemptId, questionId)?.let { questionResponseEntityToDomain(it) }

    override suspend fun insertResponse(response: QuestionResponse): Long =
        questionResponseDao.insertResponse(questionResponseDomainToEntity(response))

    override suspend fun insertResponses(responses: List<QuestionResponse>) =
        questionResponseDao.insertResponses(responses.map { questionResponseDomainToEntity(it) })

    // ==================== Question Stats ====================

    override suspend fun getStatsForQuestion(questionId: String): QuestionStats? =
        questionStatsDao.getStatsForQuestion(questionId)?.let { questionStatsEntityToDomain(it) }

    override fun getStatsForQuestionFlow(questionId: String): Flow<QuestionStats?> =
        questionStatsDao.getStatsForQuestionFlow(questionId).map { entity ->
            entity?.let { questionStatsEntityToDomain(it) }
        }

    override suspend fun getStatsForQuestions(questionIds: List<String>): List<QuestionStats> =
        questionStatsDao.getStatsForQuestions(questionIds).map { questionStatsEntityToDomain(it) }

    override fun getHardestQuestions(limit: Int): Flow<List<QuestionStats>> =
        questionStatsDao.getHardestQuestions(limit).map { entities ->
            entities.map { questionStatsEntityToDomain(it) }
        }

    override fun getEasiestQuestions(limit: Int): Flow<List<QuestionStats>> =
        questionStatsDao.getEasiestQuestions(limit).map { entities ->
            entities.map { questionStatsEntityToDomain(it) }
        }

    override suspend fun insertStats(stats: QuestionStats) =
        questionStatsDao.insertStats(questionStatsDomainToEntity(stats))

    override suspend fun updateStats(stats: QuestionStats) =
        questionStatsDao.updateStats(questionStatsDomainToEntity(stats))

    // ==================== Convenience Methods ====================

    override suspend fun startQuizAttempt(examId: String): Long {
        val attempt = QuizAttempt(
            id = 0, // Auto-generated
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
        timeSpentSeconds: Int
    ) {
        completeAttempt(attemptId, score, totalPoints, timeSpentSeconds)
    }

    override suspend fun recordQuestionResponse(
        attemptId: Long,
        questionId: String,
        userAnswer: String,
        isCorrect: Boolean,
        pointsEarned: Int,
        timeSpentSeconds: Int
    ) {
        // Create and insert the response
        val response = QuestionResponse(
            id = 0, // Auto-generated
            attemptId = attemptId,
            questionId = questionId,
            userAnswer = userAnswer,
            isCorrect = isCorrect,
            pointsEarned = pointsEarned,
            timeSpentSeconds = timeSpentSeconds,
            answeredAt = System.currentTimeMillis()
        )
        insertResponse(response)

        // Update question stats
        val currentStats = getStatsForQuestion(questionId)
        val updatedStats = if (currentStats != null) {
            currentStats.withNewResponse(isCorrect, timeSpentSeconds)
        } else {
            QuestionStats.forNewQuestion(questionId)
                .withNewResponse(isCorrect, timeSpentSeconds)
        }

        if (currentStats != null) {
            updateStats(updatedStats)
        } else {
            insertStats(updatedStats)
        }
    }
}