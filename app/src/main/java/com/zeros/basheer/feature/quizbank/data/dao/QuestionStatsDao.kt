package com.zeros.basheer.feature.quizbank.data.dao


import androidx.room.*
import com.zeros.basheer.feature.quizbank.data.entity.QuestionStatsEntity
import kotlinx.coroutines.flow.Flow

//@Dao
//interface QuestionStatsDao {
//    @Query("SELECT * FROM question_stats WHERE questionId = :questionId")
//    suspend fun getStatsForQuestion(questionId: String): QuestionStatsEntity?
//
//    @Query("SELECT * FROM question_stats WHERE questionId = :questionId")
//    fun getStatsForQuestionFlow(questionId: String): Flow<QuestionStatsEntity?>
//
//    @Query("SELECT * FROM question_stats WHERE questionId IN (:questionIds)")
//    suspend fun getStatsForQuestions(questionIds: List<String>): List<QuestionStatsEntity>
//
//    @Query("SELECT * FROM question_stats ORDER BY successRate ASC LIMIT :limit")
//    fun getHardestQuestions(limit: Int): Flow<List<QuestionStatsEntity>>
//
//    @Query("SELECT * FROM question_stats ORDER BY successRate DESC LIMIT :limit")
//    fun getEasiestQuestions(limit: Int): Flow<List<QuestionStatsEntity>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertStats(stats: QuestionStatsEntity)
//
//    @Update
//    suspend fun updateStats(stats: QuestionStatsEntity)
//}