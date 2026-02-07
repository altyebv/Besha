package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.QuestionStats
import kotlinx.coroutines.flow.Flow
@Dao
interface QuestionStatsDao {

    // ==================== Queries ====================

    @Query("SELECT * FROM question_stats WHERE questionId = :questionId")
    suspend fun getStatsForQuestion(questionId: String): QuestionStats?

    @Query("SELECT * FROM question_stats WHERE questionId = :questionId")
    fun getStatsForQuestionFlow(questionId: String): Flow<QuestionStats?>

    @Query("SELECT * FROM question_stats WHERE questionId IN (:questionIds)")
    suspend fun getStatsForQuestions(questionIds: List<String>): List<QuestionStats>

    @Query("DELETE FROM question_stats")
    suspend fun deleteAll()


    @Query("""
        SELECT * FROM question_stats 
        ORDER BY successRate ASC 
        LIMIT :limit
    """)
    fun getHardestQuestions(limit: Int = 10): Flow<List<QuestionStats>>

    @Query("""
        SELECT * FROM question_stats 
        ORDER BY successRate DESC 
        LIMIT :limit
    """)
    fun getEasiestQuestions(limit: Int = 10): Flow<List<QuestionStats>>

    @Query("""
        SELECT * FROM question_stats 
        WHERE timesAsked > 0
        ORDER BY timesAsked DESC 
        LIMIT :limit
    """)
    fun getMostAskedQuestions(limit: Int = 10): Flow<List<QuestionStats>>

    @Query("""
        SELECT * FROM question_stats 
        WHERE lastShownInFeed IS NULL 
           OR lastShownInFeed < :threshold
        ORDER BY feedShowCount ASC
    """)
    suspend fun getQuestionsNotRecentlyShown(threshold: Long): List<QuestionStats>

    // ==================== Inserts/Updates ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: QuestionStats)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllStats(stats: List<QuestionStats>)

    @Update
    suspend fun updateStats(stats: QuestionStats)

    @Query("DELETE FROM question_stats WHERE questionId = :questionId")
    suspend fun deleteStats(questionId: String)

    @Query("DELETE FROM question_stats")
    suspend fun deleteAllStats()

    // ==================== Transactional Operations ====================

    @Transaction
    suspend fun recordResponse(questionId: String, isCorrect: Boolean, timeSeconds: Int) {
        val existing = getStatsForQuestion(questionId)
        val updated = if (existing != null) {
            existing.withNewResponse(isCorrect, timeSeconds)
        } else {
            QuestionStats.forNewQuestion(questionId).withNewResponse(isCorrect, timeSeconds)
        }
        upsertStats(updated)
    }

    @Transaction
    suspend fun recordFeedShow(questionId: String) {
        val existing = getStatsForQuestion(questionId)
        val updated = if (existing != null) {
            existing.withFeedShow()
        } else {
            QuestionStats.forNewQuestion(questionId).withFeedShow()
        }
        upsertStats(updated)
    }

    @Transaction
    suspend fun recordFeedShowBatch(questionIds: List<String>) {
        questionIds.forEach { recordFeedShow(it) }
    }
}
