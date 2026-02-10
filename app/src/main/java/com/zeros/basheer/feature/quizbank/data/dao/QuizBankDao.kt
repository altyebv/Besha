package com.zeros.basheer.feature.quizbank.data.dao


import androidx.room.*
import com.zeros.basheer.feature.quizbank.data.entity.*
import com.zeros.basheer.feature.quizbank.data.relations.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY year DESC, source")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE subjectId = :subjectId ORDER BY year DESC")
    fun getExamsBySubject(subjectId: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :examId")
    suspend fun getExamById(examId: String): ExamEntity?

    @Query("SELECT * FROM exams WHERE source = :source ORDER BY year DESC")
    fun getExamsBySource(source: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE subjectId = :subjectId AND source = :source ORDER BY year DESC")
    fun getExamsBySubjectAndSource(subjectId: String, source: String): Flow<List<ExamEntity>>

    @Transaction
    @Query("SELECT * FROM exams WHERE id = :examId")
    suspend fun getExamWithQuestions(examId: String): ExamWithQuestions?

    @Transaction
    @Query("SELECT * FROM exams WHERE subjectId = :subjectId ORDER BY year DESC")
    fun getExamsWithQuestions(subjectId: String): Flow<List<ExamWithQuestions>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ExamEntity>)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE subjectId = :subjectId")
    suspend fun deleteExamsBySubject(subjectId: String)

    @Query("DELETE FROM exams")
    suspend fun deleteAll()
}

@Dao
interface ExamQuestionDao {
    @Query("SELECT * FROM exam_questions WHERE examId = :examId ORDER BY `order`")
    fun getByExamId(examId: String): Flow<List<ExamQuestionEntity>>

    @Query("SELECT * FROM exam_questions WHERE questionId = :questionId")
    fun getByQuestionId(questionId: String): Flow<List<ExamQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(examQuestion: ExamQuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(examQuestions: List<ExamQuestionEntity>)

    @Delete
    suspend fun delete(examQuestion: ExamQuestionEntity)

    @Query("DELETE FROM exam_questions WHERE examId = :examId")
    suspend fun deleteByExamId(examId: String)
}

@Dao
interface QuestionConceptDao {
    @Query("SELECT * FROM question_concepts WHERE questionId = :questionId")
    fun getByQuestionId(questionId: String): Flow<List<QuestionConceptEntity>>

    @Query("SELECT * FROM question_concepts WHERE conceptId = :conceptId")
    fun getByConceptId(conceptId: String): Flow<List<QuestionConceptEntity>>

    @Query("SELECT conceptId FROM question_concepts WHERE questionId IN (:questionIds)")
    suspend fun getConceptsForQuestions(questionIds: List<String>): List<String>

    @Query("SELECT questionId FROM question_concepts WHERE conceptId IN (:conceptIds)")
    suspend fun getQuestionsForConcepts(conceptIds: List<String>): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(questionConcept: QuestionConceptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questionConcepts: List<QuestionConceptEntity>)

    @Delete
    suspend fun delete(questionConcept: QuestionConceptEntity)

    @Query("DELETE FROM question_concepts WHERE questionId = :questionId")
    suspend fun deleteByQuestionId(questionId: String)
}

@Dao
interface QuizAttemptDao {
    @Query("SELECT * FROM quiz_attempts WHERE examId = :examId ORDER BY startedAt DESC")
    fun getAttemptsByExam(examId: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE id = :attemptId")
    suspend fun getAttemptById(attemptId: Long): QuizAttemptEntity?

    @Query("SELECT * FROM quiz_attempts ORDER BY startedAt DESC LIMIT :limit")
    fun getRecentAttempts(limit: Int = 10): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE examId = :examId ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLastAttemptForExam(examId: String): QuizAttemptEntity?

    @Transaction
    @Query("SELECT * FROM quiz_attempts WHERE id = :attemptId")
    suspend fun getAttemptWithResponses(attemptId: Long): QuizAttemptWithResponses?

    @Insert
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    @Update
    suspend fun updateAttempt(attempt: QuizAttemptEntity)

    @Delete
    suspend fun deleteAttempt(attempt: QuizAttemptEntity)

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

@Dao
interface QuestionResponseDao {
    @Query("SELECT * FROM question_responses WHERE attemptId = :attemptId")
    fun getResponsesByAttempt(attemptId: Long): Flow<List<QuestionResponseEntity>>

    @Query("SELECT * FROM question_responses WHERE questionId = :questionId")
    fun getResponsesByQuestion(questionId: String): Flow<List<QuestionResponseEntity>>

    @Query("SELECT * FROM question_responses WHERE attemptId = :attemptId AND questionId = :questionId")
    suspend fun getResponse(attemptId: Long, questionId: String): QuestionResponseEntity?

    @Insert
    suspend fun insertResponse(response: QuestionResponseEntity): Long

    @Insert
    suspend fun insertResponses(responses: List<QuestionResponseEntity>)

    @Update
    suspend fun updateResponse(response: QuestionResponseEntity)

    @Delete
    suspend fun deleteResponse(response: QuestionResponseEntity)

    @Query("DELETE FROM question_responses WHERE attemptId = :attemptId")
    suspend fun deleteResponsesByAttempt(attemptId: Long)
}

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