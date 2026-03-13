package com.zeros.basheer.feature.quizbank.data.dao


import androidx.room.*
import com.zeros.basheer.feature.quizbank.data.entity.QuestionStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionStatsDao {
    @Query("SELECT * FROM question_stats WHERE questionId = :questionId")
    suspend fun getStatsForQuestion(questionId: String): QuestionStatsEntity?

    @Query("SELECT * FROM question_stats WHERE questionId = :questionId")
    fun getStatsForQuestionFlow(questionId: String): Flow<QuestionStatsEntity?>

    @Query("SELECT * FROM question_stats WHERE questionId IN (:questionIds)")
    suspend fun getStatsForQuestions(questionIds: List<String>): List<QuestionStatsEntity>

    @Query("""
        SELECT * FROM question_stats 
        ORDER BY successRate ASC 
        LIMIT :limit
    """)
    fun getHardestQuestions(limit: Int = 10): Flow<List<QuestionStatsEntity>>

    @Query("""
        SELECT * FROM question_stats 
        ORDER BY successRate DESC 
        LIMIT :limit
    """)
    fun getEasiestQuestions(limit: Int = 10): Flow<List<QuestionStatsEntity>>

    @Query("""
        SELECT * FROM question_stats 
        WHERE timesAsked > 0
        ORDER BY (timesAsked - feedShowCount) DESC
        LIMIT :limit
    """)
    fun getQuestionsForFeed(limit: Int = 20): Flow<List<QuestionStatsEntity>>


    /**
     * Returns questions with a below-threshold success rate for a specific subject,
     * ranked by a weakness score: (1 - successRate) * timesAsked.
     *
     * This formula naturally surfaces questions that are BOTH hard AND frequently
     * seen — a question answered wrong once has less signal than one answered wrong
     * 8 times. Questions with fewer than [minAttempts] attempts are excluded because
     * there is not enough signal to call them weak yet.
     *
     * Used by [GetWeakAreaQuestionsUseCase] to populate WEAK_AREAS practice sessions.
     */
    @Query("""
        SELECT qs.* FROM question_stats qs
        INNER JOIN questions q ON qs.questionId = q.id
        WHERE q.subjectId = :subjectId
        AND q.isCheckpoint = 0
        AND qs.timesAsked >= :minAttempts
        AND qs.successRate < :threshold
        ORDER BY ((1.0 - qs.successRate) * qs.timesAsked) DESC
        LIMIT :limit
    """)
    suspend fun getWeakQuestionsForSubject(
        subjectId: String,
        minAttempts: Int,
        threshold: Float,
        limit: Int
    ): List<QuestionStatsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: QuestionStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatsList(statsList: List<QuestionStatsEntity>)

    @Update
    suspend fun updateStats(stats: QuestionStatsEntity)

    @Delete
    suspend fun deleteStats(stats: QuestionStatsEntity)

    @Query("DELETE FROM question_stats")
    suspend fun deleteAll()
}