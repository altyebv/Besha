package com.zeros.basheer.feature.practice.data.dao

import androidx.room.*
import com.zeros.basheer.feature.practice.data.entity.PracticeQuestionEntity
import com.zeros.basheer.feature.practice.data.entity.PracticeSessionEntity
import com.zeros.basheer.feature.practice.domain.model.PracticeGenerationType
import com.zeros.basheer.feature.practice.domain.model.PracticeQuestion
import com.zeros.basheer.feature.practice.domain.model.PracticeSessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeSessionDao {
    
    // ==================== Session Queries ====================
    
    @Query("SELECT * FROM practice_sessions WHERE id = :sessionId")
    suspend fun getSession(sessionId: Long): PracticeSessionEntity?
    
    @Query("SELECT * FROM practice_sessions WHERE id = :sessionId")
    fun getSessionFlow(sessionId: Long): Flow<PracticeSessionEntity?>

    @Query("Delete FROM practice_sessions")
    suspend fun deleteAll()
    
    @Query("""
        SELECT * FROM practice_sessions 
        WHERE subjectId = :subjectId 
        ORDER BY startedAt DESC
    """)
    fun getSessionsBySubject(subjectId: String): Flow<List<PracticeSessionEntity>>
    
    @Query("""
        SELECT * FROM practice_sessions 
        WHERE status = :status
        ORDER BY startedAt DESC
    """)
    fun getSessionsByStatus(status: PracticeSessionStatus): Flow<List<PracticeSessionEntity>>
    
    @Query("""
        SELECT * FROM practice_sessions 
        WHERE status = 'IN_PROGRESS' OR status = 'PAUSED'
        ORDER BY startedAt DESC
        LIMIT 1
    """)
    suspend fun getActiveSession(): PracticeSessionEntity?
    
    @Query("""
        SELECT * FROM practice_sessions 
        WHERE status = 'COMPLETED'
        ORDER BY startedAt DESC
        LIMIT :limit
    """)
    fun getRecentCompletedSessions(limit: Int = 10): Flow<List<PracticeSessionEntity>>
    
    @Query("""
        SELECT * FROM practice_sessions 
        WHERE generationType = :type
        ORDER BY startedAt DESC
    """)
    fun getSessionsByType(type: PracticeGenerationType): Flow<List<PracticeSessionEntity>>
    
    @Query("""
        SELECT AVG(score) FROM practice_sessions 
        WHERE subjectId = :subjectId 
        AND status = 'COMPLETED'
        AND score IS NOT NULL
    """)
    fun getAverageScore(subjectId: String): Flow<Float?>
    
    @Query("""
        SELECT COUNT(*) FROM practice_sessions 
        WHERE subjectId = :subjectId 
        AND status = 'COMPLETED'
    """)
    fun getCompletedSessionCount(subjectId: String): Flow<Int>
    
    // ==================== Session Mutations ====================
    
    @Insert
    suspend fun insertSession(session: PracticeSessionEntity): Long
    
    @Update
    suspend fun updateSession(session: PracticeSessionEntity)
    
    @Query("DELETE FROM practice_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
    
    @Query("""
        UPDATE practice_sessions 
        SET status = :status, completedAt = :completedAt 
        WHERE id = :sessionId
    """)
    suspend fun updateSessionStatus(
        sessionId: Long, 
        status: PracticeSessionStatus, 
        completedAt: Long? = null
    )
    
    @Query("""
        UPDATE practice_sessions 
        SET currentQuestionIndex = :index 
        WHERE id = :sessionId
    """)
    suspend fun updateCurrentQuestion(sessionId: Long, index: Int)
    
    @Query("""
        UPDATE practice_sessions 
        SET correctCount = correctCount + 1 
        WHERE id = :sessionId
    """)
    suspend fun incrementCorrectCount(sessionId: Long)
    
    @Query("""
        UPDATE practice_sessions 
        SET wrongCount = wrongCount + 1 
        WHERE id = :sessionId
    """)
    suspend fun incrementWrongCount(sessionId: Long)
    
    @Query("""
        UPDATE practice_sessions 
        SET skippedCount = skippedCount + 1 
        WHERE id = :sessionId
    """)
    suspend fun incrementSkippedCount(sessionId: Long)
    
    // ==================== Practice Questions ====================
    
    @Query("""
        SELECT * FROM practice_questions 
        WHERE sessionId = :sessionId 
        ORDER BY `order`
    """)
    suspend fun getQuestionsForSession(sessionId: Long): List<PracticeQuestionEntity>
    
    @Query("""
        SELECT * FROM practice_questions 
        WHERE sessionId = :sessionId 
        ORDER BY `order`
    """)
    fun getQuestionsForSessionFlow(sessionId: Long): Flow<List<PracticeQuestionEntity>>
    
    @Query("""
        SELECT * FROM practice_questions 
        WHERE sessionId = :sessionId AND `order` = :order
    """)
    suspend fun getQuestionAtIndex(sessionId: Long, order: Int): PracticeQuestionEntity?
    
    @Query("""
        SELECT * FROM practice_questions 
        WHERE sessionId = :sessionId AND userAnswer IS NULL AND skipped = 0
        ORDER BY `order`
        LIMIT 1
    """)
    suspend fun getNextUnansweredQuestion(sessionId: Long): PracticeQuestionEntity?
    
    @Query("""
        SELECT COUNT(*) FROM practice_questions 
        WHERE sessionId = :sessionId AND userAnswer IS NOT NULL
    """)
    suspend fun getAnsweredCount(sessionId: Long): Int
    
    @Insert
    suspend fun insertPracticeQuestion(question: PracticeQuestionEntity)
    
    @Insert
    suspend fun insertPracticeQuestions(questions: List<PracticeQuestionEntity>)
    
    @Update
    suspend fun updatePracticeQuestion(question: PracticeQuestionEntity)
    
    @Query("""
        UPDATE practice_questions 
        SET userAnswer = :answer, isCorrect = :isCorrect, 
            timeSpentSeconds = :timeSeconds, answeredAt = :answeredAt
        WHERE sessionId = :sessionId AND questionId = :questionId
    """)
    suspend fun recordAnswer(
        sessionId: Long,
        questionId: String,
        answer: String,
        isCorrect: Boolean,
        timeSeconds: Int,
        answeredAt: Long = System.currentTimeMillis()
    )
    
    @Query("""
        UPDATE practice_questions 
        SET skipped = 1
        WHERE sessionId = :sessionId AND questionId = :questionId
    """)
    suspend fun markQuestionSkipped(sessionId: Long, questionId: String)
    
    // ==================== Transactional Operations ====================
    
    /**
     * Complete a session and calculate final score.
     */
    @Transaction
    suspend fun completeSession(sessionId: Long) {
        val session = getSession(sessionId) ?: return
        val questions = getQuestionsForSession(sessionId)
        
        val answered = questions.count { it.userAnswer != null }
        val correct = questions.count { it.isCorrect == true }
        val totalTime = questions.sumOf { it.timeSpentSeconds ?: 0 }
        
        val score = if (answered > 0) {
            (correct.toFloat() / session.questionCount) * 100
        } else {
            0f
        }
        
        updateSession(
            session.copy(
                status = PracticeSessionStatus.COMPLETED.toString(),
                completedAt = System.currentTimeMillis(),
                correctCount = correct,
                wrongCount = questions.count { it.isCorrect == false },
                skippedCount = questions.count { it.skipped },
                score = score,
                totalTimeSeconds = totalTime
            )
        )
    }
    
    /**
     * Create a new session with questions.
     */
    @Transaction
    suspend fun createSessionWithQuestions(
        session: PracticeSessionEntity,
        questionIds: List<String>
    ): Long {
        val sessionId = insertSession(session)
        
        val practiceQuestions = questionIds.mapIndexed { index, questionId ->
            PracticeQuestionEntity(
                sessionId = sessionId,
                questionId = questionId,
                order = index
            )
        }
        
        insertPracticeQuestions(practiceQuestions)
        return sessionId
    }
}
