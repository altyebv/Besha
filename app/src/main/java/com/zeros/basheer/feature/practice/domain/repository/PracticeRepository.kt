package com.zeros.basheer.feature.practice.domain.repository


import com.zeros.basheer.feature.practice.domain.model.*
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import kotlinx.coroutines.flow.Flow

interface PracticeRepository {

    // ==================== Session Queries ====================

    suspend fun getSession(sessionId: Long): PracticeSession?
    fun getSessionFlow(sessionId: Long): Flow<PracticeSession?>
    fun getSessionsBySubject(subjectId: String): Flow<List<PracticeSession>>
    suspend fun getActiveSession(): PracticeSession?
    fun getRecentCompletedSessions(limit: Int = 10): Flow<List<PracticeSession>>
    fun getAverageScore(subjectId: String): Flow<Float?>
    fun getCompletedSessionCount(subjectId: String): Flow<Int>

    // ==================== Practice Questions ====================

    suspend fun getQuestionsForSession(sessionId: Long): List<PracticeQuestion>
    fun getQuestionsForSessionFlow(sessionId: Long): Flow<List<PracticeQuestion>>
    suspend fun getNextUnansweredQuestion(sessionId: Long): PracticeQuestion?

    // ==================== Session Creation & Management ====================

    suspend fun createPracticeSession(
        subjectId: String,
        generationType: PracticeGenerationType,
        questionCount: Int = 20,
        filterUnitIds: List<String>? = null,
        filterLessonIds: List<String>? = null,
        filterConceptIds: List<String>? = null,
        filterQuestionTypes: List<QuestionType>? = null,
        filterDifficulty: IntRange? = null,
        filterSource: String? = null
    ): Long


    /**
     * Create a practice session from an explicit, pre-ranked list of question IDs.
     *
     * Use this instead of [createPracticeSession] when the caller has already done
     * the selection logic (e.g. [GetWeakAreaQuestionsUseCase]) and just needs a
     * session container. The question order is preserved — pass them ranked by
     * priority if ordering matters (weak areas: worst first).
     */
    suspend fun createSessionFromQuestionIds(
        subjectId: String,
        questionIds: List<String>,
        generationType: PracticeGenerationType = PracticeGenerationType.WEAK_AREAS,
        shuffled: Boolean = true
    ): Long

    suspend fun recordAnswer(
        sessionId: Long,
        questionId: String,
        answer: String,
        isCorrect: Boolean,
        timeSeconds: Int
    )

    suspend fun skipQuestion(sessionId: Long, questionId: String)

    suspend fun completeSession(sessionId: Long)

    suspend fun updateSessionStatus(
        sessionId: Long,
        status: PracticeSessionStatus,
        completedAt: Long? = null
    )
}