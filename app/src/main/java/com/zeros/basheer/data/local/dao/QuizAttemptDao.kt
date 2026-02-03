package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.QuizAttempt
import com.zeros.basheer.data.relations.QuizAttemptWithResponses
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAttemptDao {
    @Query("SELECT * FROM quiz_attempts WHERE examId = :examId ORDER BY startedAt DESC")
    fun getAttemptsByExam(examId: String): Flow<List<QuizAttempt>>

    @Query("SELECT * FROM quiz_attempts WHERE id = :attemptId")
    suspend fun getAttemptById(attemptId: Long): QuizAttempt?

    @Query("SELECT * FROM quiz_attempts ORDER BY startedAt DESC LIMIT :limit")
    fun getRecentAttempts(limit: Int = 10): Flow<List<QuizAttempt>>

    @Query("SELECT * FROM quiz_attempts WHERE examId = :examId ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLastAttemptForExam(examId: String): QuizAttempt?

    @Transaction
    @Query("SELECT * FROM quiz_attempts WHERE id = :attemptId")
    suspend fun getAttemptWithResponses(attemptId: Long): QuizAttemptWithResponses?

    @Insert
    suspend fun insertAttempt(attempt: QuizAttempt): Long

    @Update
    suspend fun updateAttempt(attempt: QuizAttempt)

    @Delete
    suspend fun deleteAttempt(attempt: QuizAttempt)

    @Query("DELETE FROM quiz_attempts WHERE examId = :examId")
    suspend fun deleteAttemptsByExam(examId: String)

    @Transaction
    suspend fun completeAttempt(attemptId: Long, score: Int, totalPoints: Int, timeSpentSeconds: Int) {
        val attempt = getAttemptById(attemptId)
        attempt?.let {
            updateAttempt(
                it.copy(
                    completedAt = System.currentTimeMillis(),
                    score = score,
                    totalPoints = totalPoints,
                    percentage = if (totalPoints > 0) score.toFloat() / totalPoints * 100 else 0f,
                    timeSpentSeconds = timeSpentSeconds
                )
            )
        }
    }
}
