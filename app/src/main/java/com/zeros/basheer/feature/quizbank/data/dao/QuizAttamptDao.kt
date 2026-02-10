package com.zeros.basheer.feature.quizbank.data.dao


import androidx.room.*
import com.zeros.basheer.feature.quizbank.data.entity.QuizAttemptEntity
import kotlinx.coroutines.flow.Flow
//
//@Dao
//interface QuizAttemptDao {
//    @Query("SELECT * FROM quiz_attempts WHERE examId = :examId ORDER BY startedAt DESC")
//    fun getAttemptsByExam(examId: String): Flow<List<QuizAttemptEntity>>
//
//    @Query("SELECT * FROM quiz_attempts WHERE id = :attemptId")
//    suspend fun getAttemptById(attemptId: Long): QuizAttemptEntity?
//
//    @Query("SELECT * FROM quiz_attempts ORDER BY startedAt DESC LIMIT :limit")
//    fun getRecentAttempts(limit: Int): Flow<List<QuizAttemptEntity>>
//
//    @Query("SELECT * FROM quiz_attempts WHERE examId = :examId ORDER BY startedAt DESC LIMIT 1")
//    suspend fun getLastAttemptForExam(examId: String): QuizAttemptEntity?
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long
//
//    @Update
//    suspend fun updateAttempt(attempt: QuizAttemptEntity)
//
//    @Query("""
//        UPDATE quiz_attempts
//        SET completedAt = :completedAt,
//            score = :score,
//            totalPoints = :totalPoints,
//            percentage = CAST(:score AS REAL) / :totalPoints * 100,
//            timeSpentSeconds = :timeSpentSeconds
//        WHERE id = :attemptId
//    """)
//    suspend fun completeAttempt(
//        attemptId: Long,
//        score: Int,
//        totalPoints: Int,
//        timeSpentSeconds: Int,
//        completedAt: Long = System.currentTimeMillis()
//    )
//
//    @Query("DELETE FROM quiz_attempts WHERE examId = :examId")
//    suspend fun deleteAttemptsByExam(examId: String)
//}