package com.zeros.basheer.feature.quizbank.data.dao

import androidx.room.*
import com.zeros.basheer.feature.quizbank.data.entity.QuestionResponseEntity
import kotlinx.coroutines.flow.Flow

//@Dao
//interface QuestionResponseDao {
//    @Query("SELECT * FROM question_responses WHERE attemptId = :attemptId ORDER BY answeredAt")
//    fun getResponsesByAttempt(attemptId: Long): Flow<List<QuestionResponseEntity>>
//
//    @Query("SELECT * FROM question_responses WHERE attemptId = :attemptId AND questionId = :questionId")
//    suspend fun getResponse(attemptId: Long, questionId: String): QuestionResponseEntity?
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertResponse(response: QuestionResponseEntity): Long
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertResponses(responses: List<QuestionResponseEntity>)
//}