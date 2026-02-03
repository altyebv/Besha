package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.Question
import com.zeros.basheer.data.models.QuestionType
import com.zeros.basheer.data.relations.QuestionWithConcepts
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE subjectId = :subjectId")
    fun getQuestionsBySubject(subjectId: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE unitId = :unitId")
    fun getQuestionsByUnit(unitId: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: String): Question?

    @Query("SELECT * FROM questions WHERE type = :type")
    fun getQuestionsByType(type: QuestionType): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId AND type = :type")
    fun getQuestionsBySubjectAndType(subjectId: String, type: QuestionType): Flow<List<Question>>

    // Get questions by concept
    @Query("""
        SELECT q.* FROM questions q
        INNER JOIN question_concepts qc ON q.id = qc.questionId
        WHERE qc.conceptId = :conceptId
    """)
    fun getQuestionsByConcept(conceptId: String): Flow<List<Question>>

    // Get questions for multiple concepts (for feeds)
    @Query("""
        SELECT q.* FROM questions q
        INNER JOIN question_concepts qc ON q.id = qc.questionId
        WHERE qc.conceptId IN (:conceptIds)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getQuestionsForConcepts(conceptIds: List<String>, limit: Int): List<Question>

    // Filter questions
    @Query("""
        SELECT DISTINCT q.* FROM questions q
        LEFT JOIN question_concepts qc ON q.id = qc.questionId
        WHERE q.subjectId = :subjectId
        AND (:unitId IS NULL OR q.unitId = :unitId)
        AND (:type IS NULL OR q.type = :type)
        AND (:conceptId IS NULL OR qc.conceptId = :conceptId)
        AND (:minDifficulty IS NULL OR q.difficulty >= :minDifficulty)
        AND (:maxDifficulty IS NULL OR q.difficulty <= :maxDifficulty)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getFilteredQuestions(
        subjectId: String,
        unitId: String? = null,
        type: QuestionType? = null,
        conceptId: String? = null,
        minDifficulty: Int? = null,
        maxDifficulty: Int? = null,
        limit: Int = 20
    ): List<Question>

    @Transaction
    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionWithConcepts(questionId: String): QuestionWithConcepts?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Query("DELETE FROM questions WHERE subjectId = :subjectId")
    suspend fun deleteQuestionsBySubject(subjectId: String)
}
