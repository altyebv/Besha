package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.Question
import com.zeros.basheer.data.models.QuestionSource
import com.zeros.basheer.data.models.QuestionType
import com.zeros.basheer.data.relations.QuestionWithConcepts
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    
    // ==================== Basic Queries ====================
    
    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: String): Question?
    
    @Query("SELECT * FROM questions WHERE id = :questionId")
    fun getQuestionByIdFlow(questionId: String): Flow<Question?>
    
    @Query("SELECT * FROM questions WHERE subjectId = :subjectId")
    fun getQuestionsBySubject(subjectId: String): Flow<List<Question>>
    
    @Query("SELECT * FROM questions WHERE unitId = :unitId")
    fun getQuestionsByUnit(unitId: String): Flow<List<Question>>
    
    @Query("SELECT * FROM questions WHERE lessonId = :lessonId")
    fun getQuestionsByLesson(lessonId: String): Flow<List<Question>>


    
    // ==================== Type-Based Queries ====================
    
    @Query("SELECT * FROM questions WHERE type = :type")
    fun getQuestionsByType(type: QuestionType): Flow<List<Question>>
    
    @Query("SELECT * FROM questions WHERE subjectId = :subjectId AND type = :type")
    fun getQuestionsByType(subjectId: String, type: QuestionType): Flow<List<Question>>
    
    @Query("SELECT * FROM questions WHERE subjectId = :subjectId AND feedEligible = 1")
    fun getFeedEligibleQuestions(subjectId: String): Flow<List<Question>>
    
    // ==================== Source-Based Queries ====================
    
    @Query("SELECT * FROM questions WHERE source = :source")
    fun getQuestionsBySource(source: QuestionSource): Flow<List<Question>>
    
    @Query("SELECT * FROM questions WHERE subjectId = :subjectId AND source = :source")
    fun getQuestionsBySource(subjectId: String, source: QuestionSource): Flow<List<Question>>
    
    @Query("SELECT * FROM questions WHERE sourceExamId = :examId ORDER BY id")
    fun getQuestionsBySourceExam(examId: String): Flow<List<Question>>
    
    // ==================== Concept-Based Queries ====================
    
    @Query("""
        SELECT q.* FROM questions q
        INNER JOIN question_concepts qc ON q.id = qc.questionId
        WHERE qc.conceptId = :conceptId
    """)
    fun getQuestionsByConcept(conceptId: String): Flow<List<Question>>
    
    @Query("""
        SELECT DISTINCT q.* FROM questions q
        INNER JOIN question_concepts qc ON q.id = qc.questionId
        WHERE q.subjectId = :subjectId AND qc.conceptId IN (:conceptIds)
    """)
    fun getQuestionsByConceptIds(subjectId: String, conceptIds: List<String>): Flow<List<Question>>
    
    @Query("""
        SELECT q.* FROM questions q
        INNER JOIN question_concepts qc ON q.id = qc.questionId
        WHERE qc.conceptId IN (:conceptIds)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getQuestionsForConcepts(conceptIds: List<String>, limit: Int): List<Question>
    
    // ==================== Unit-Based Queries ====================
    
    @Query("SELECT * FROM questions WHERE subjectId = :subjectId AND unitId IN (:unitIds)")
    fun getQuestionsByUnitIds(subjectId: String, unitIds: List<String>): Flow<List<Question>>
    
    // ==================== Exam-Based Queries ====================
    
    @Query("""
        SELECT q.* FROM questions q
        INNER JOIN exam_questions eq ON q.id = eq.questionId
        WHERE eq.examId = :examId
        ORDER BY eq.`order`
    """)
    suspend fun getQuestionsForExam(examId: String): List<Question>
    
    @Query("""
        SELECT q.* FROM questions q
        INNER JOIN exam_questions eq ON q.id = eq.questionId
        WHERE eq.examId = :examId
        ORDER BY eq.`order`
    """)
    fun getQuestionsForExamFlow(examId: String): Flow<List<Question>>
    
    // ==================== Filtered Queries ====================
    
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
    
    // ==================== Counts ====================
    
    @Query("SELECT COUNT(*) FROM questions WHERE subjectId = :subjectId")
    fun getQuestionCount(subjectId: String): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM questions WHERE unitId = :unitId")
    fun getQuestionCountByUnit(unitId: String): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM questions WHERE subjectId = :subjectId AND type = :type")
    fun getQuestionCountByType(subjectId: String, type: QuestionType): Flow<Int>
    
    // ==================== Relations ====================
    
    @Transaction
    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionWithConcepts(questionId: String): QuestionWithConcepts?
    
    // ==================== Mutations ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)
    
    @Update
    suspend fun updateQuestion(question: Question)
    
    @Delete
    suspend fun deleteQuestion(question: Question)

    @Query("DELETE FROM questions")
    suspend fun deleteAll()
    
    @Query("DELETE FROM questions WHERE subjectId = :subjectId")
    suspend fun deleteQuestionsBySubject(subjectId: String)
    
    @Query("DELETE FROM questions WHERE id = :questionId")
    suspend fun deleteQuestionById(questionId: String)
}
