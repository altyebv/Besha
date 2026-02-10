package com.zeros.basheer.feature.practice.domain.repository


import com.zeros.basheer.feature.practice.data.dao.PracticeSessionDao
import com.zeros.basheer.feature.practice.data.entity.PracticeQuestionEntity
import com.zeros.basheer.feature.practice.data.entity.PracticeSessionEntity
import com.zeros.basheer.feature.practice.domain.model.*
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PracticeRepositoryImpl @Inject constructor(
    private val practiceSessionDao: PracticeSessionDao,
    private val quizBankRepository: QuizBankRepository
) : PracticeRepository {

    // ==================== Mappers ====================

    private fun entityToDomain(entity: PracticeSessionEntity): PracticeSession = PracticeSession(
        id = entity.id,
        subjectId = entity.subjectId,
        generationType = PracticeGenerationType.valueOf(entity.generationType),
        filterUnitIds = entity.filterUnitIds ?: "",
        filterLessonIds = entity.filterLessonIds ?: "",
        filterConceptIds = entity.filterConceptIds ?: "",
        filterQuestionTypes = (entity.filterQuestionTypes?.split(",") ?: emptyList()).toString(),
        filterDifficulty = entity.filterDifficulty?.let {
            // Parse "1..3" format
            val parts = it.split("..")
            if (parts.size == 2) parts[0].toInt()..parts[1].toInt() else null
        }.toString(),
        filterSource = entity.filterSource,
        questionCount = entity.questionCount,
        timeLimitSeconds = entity.timeLimitSeconds,
        shuffled = entity.shuffled,
        status = PracticeSessionStatus.valueOf(entity.status),
        currentQuestionIndex = entity.currentQuestionIndex,
        correctCount = entity.correctCount,
        wrongCount = entity.wrongCount,
        skippedCount = entity.skippedCount,
        score = entity.score,
        startedAt = entity.startedAt,
        completedAt = entity.completedAt,
        totalTimeSeconds = entity.totalTimeSeconds
    )

    private fun practiceQuestionEntityToDomain(entity: PracticeQuestionEntity): PracticeQuestion =
        PracticeQuestion(
            sessionId = entity.sessionId,
            questionId = entity.questionId,
            order = entity.order,
            userAnswer = entity.userAnswer,
            isCorrect = entity.isCorrect,
            timeSpentSeconds = entity.timeSpentSeconds,
            answeredAt = entity.answeredAt,
            skipped = entity.skipped
        )

    // ==================== Implementation ====================

    override suspend fun getSession(sessionId: Long): PracticeSession? =
        practiceSessionDao.getSession(sessionId)?.let { entityToDomain(it) }

    override fun getSessionFlow(sessionId: Long): Flow<PracticeSession?> =
        practiceSessionDao.getSessionFlow(sessionId).map { it?.let { entityToDomain(it) } }

    override fun getSessionsBySubject(subjectId: String): Flow<List<PracticeSession>> =
        practiceSessionDao.getSessionsBySubject(subjectId).map { sessions ->
            sessions.map { entityToDomain(it) }
        }

    override suspend fun getActiveSession(): PracticeSession? =
        practiceSessionDao.getActiveSession()?.let { entityToDomain(it) }

    override fun getRecentCompletedSessions(limit: Int): Flow<List<PracticeSession>> =
        practiceSessionDao.getRecentCompletedSessions(limit).map { sessions ->
            sessions.map { entityToDomain(it) }
        }

    override fun getAverageScore(subjectId: String): Flow<Float?> =
        practiceSessionDao.getAverageScore(subjectId)

    override fun getCompletedSessionCount(subjectId: String): Flow<Int> =
        practiceSessionDao.getCompletedSessionCount(subjectId)

    override suspend fun getQuestionsForSession(sessionId: Long): List<PracticeQuestion> =
        practiceSessionDao.getQuestionsForSession(sessionId).map { practiceQuestionEntityToDomain(it) }

    override fun getQuestionsForSessionFlow(sessionId: Long): Flow<List<PracticeQuestion>> =
        practiceSessionDao.getQuestionsForSessionFlow(sessionId).map { questions ->
            questions.map { practiceQuestionEntityToDomain(it) }
        }

    override suspend fun getNextUnansweredQuestion(sessionId: Long): PracticeQuestion? =
        practiceSessionDao.getNextUnansweredQuestion(sessionId)?.let { practiceQuestionEntityToDomain(it) }

    override suspend fun createPracticeSession(
        subjectId: String,
        generationType: PracticeGenerationType,
        questionCount: Int,
        filterUnitIds: List<String>?,
        filterLessonIds: List<String>?,
        filterConceptIds: List<String>?,
        filterQuestionTypes: List<QuestionType>?,
        filterDifficulty: IntRange?,
        filterSource: String?
    ): Long {
        // Get questions based on filters
        val questions = quizBankRepository.getFilteredQuestions(
            subjectId = subjectId,
            unitId = filterUnitIds?.firstOrNull(),
            type = filterQuestionTypes?.firstOrNull(),
            conceptId = filterConceptIds?.firstOrNull(),
            minDifficulty = filterDifficulty?.first,
            maxDifficulty = filterDifficulty?.last,
            limit = questionCount
        )

        // Create session entity
        val session = PracticeSessionEntity(
            subjectId = subjectId,
            generationType = generationType.name,
            filterUnitIds = filterUnitIds?.joinToString(",") ?: "",
            filterLessonIds = filterLessonIds?.joinToString(",") ?: "",
            filterConceptIds = filterConceptIds?.joinToString(",") ?: "",
            filterQuestionTypes = filterQuestionTypes?.joinToString(",") { it.name },
            filterDifficulty = filterDifficulty?.let { "${it.first}..${it.last}" },
            filterSource = filterSource,
            questionCount = questions.size,
            shuffled = true
        )

        return practiceSessionDao.createSessionWithQuestions(
            session = session,
            questionIds = questions.map { it.id }
        )
    }

    override suspend fun recordAnswer(
        sessionId: Long,
        questionId: String,
        answer: String,
        isCorrect: Boolean,
        timeSeconds: Int
    ) {
        practiceSessionDao.recordAnswer(sessionId, questionId, answer, isCorrect, timeSeconds)

        if (isCorrect) {
            practiceSessionDao.incrementCorrectCount(sessionId)
        } else {
            practiceSessionDao.incrementWrongCount(sessionId)
        }

        // Move to next question
        val session = practiceSessionDao.getSession(sessionId)
        session?.let {
            practiceSessionDao.updateCurrentQuestion(sessionId, it.currentQuestionIndex + 1)
        }
    }

    override suspend fun skipQuestion(sessionId: Long, questionId: String) {
        practiceSessionDao.markQuestionSkipped(sessionId, questionId)
        practiceSessionDao.incrementSkippedCount(sessionId)

        val session = practiceSessionDao.getSession(sessionId)
        session?.let {
            practiceSessionDao.updateCurrentQuestion(sessionId, it.currentQuestionIndex + 1)
        }
    }

    override suspend fun completeSession(sessionId: Long) {
        practiceSessionDao.completeSession(sessionId)
    }

    override suspend fun updateSessionStatus(
        sessionId: Long,
        status: PracticeSessionStatus,
        completedAt: Long?
    ) {
        practiceSessionDao.updateSessionStatus(
            sessionId = sessionId,
            status = status,
            completedAt = completedAt
        )
    }
}