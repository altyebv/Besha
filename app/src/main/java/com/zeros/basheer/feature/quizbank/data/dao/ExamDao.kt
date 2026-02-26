package com.zeros.basheer.feature.quizbank.data.dao
//

import androidx.room.*
import com.zeros.basheer.feature.quizbank.data.entity.ExamEntity
import com.zeros.basheer.feature.quizbank.data.entity.ExamQuestionEntity
import com.zeros.basheer.feature.quizbank.data.relations.ExamWithQuestions
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
