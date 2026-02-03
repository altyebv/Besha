package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.QuestionResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionResponseDao {
    @Query("SELECT * FROM question_responses WHERE attemptId = :attemptId")
    fun getResponsesByAttempt(attemptId: Long): Flow<List<QuestionResponse>>

    @Query("SELECT * FROM question_responses WHERE questionId = :questionId")
    fun getResponsesByQuestion(questionId: String): Flow<List<QuestionResponse>>

    @Query("SELECT * FROM question_responses WHERE attemptId = :attemptId AND questionId = :questionId")
    suspend fun getResponse(attemptId: Long, questionId: String): QuestionResponse?

    // Get accuracy stats for a question
    @Query("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) as correct
        FROM question_responses 
        WHERE questionId = :questionId
    """)
    suspend fun getQuestionStats(questionId: String): QuestionStats

    @Insert
    suspend fun insertResponse(response: QuestionResponse): Long

    @Insert
    suspend fun insertResponses(responses: List<QuestionResponse>)

    @Update
    suspend fun updateResponse(response: QuestionResponse)

    @Delete
    suspend fun deleteResponse(response: QuestionResponse)

    @Query("DELETE FROM question_responses WHERE attemptId = :attemptId")
    suspend fun deleteResponsesByAttempt(attemptId: Long)
}

data class QuestionStats(
    val total: Int,
    val correct: Int
) {
    val accuracy: Float get() = if (total > 0) correct.toFloat() / total else 0f
}
