package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.Exam
import com.zeros.basheer.data.models.ExamSource
import com.zeros.basheer.data.relations.ExamWithQuestions
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY year DESC, source")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE subjectId = :subjectId ORDER BY year DESC")
    fun getExamsBySubject(subjectId: String): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE id = :examId")
    suspend fun getExamById(examId: String): Exam?

    @Query("SELECT * FROM exams WHERE source = :source ORDER BY year DESC")
    fun getExamsBySource(source: ExamSource): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE subjectId = :subjectId AND source = :source ORDER BY year DESC")
    fun getExamsBySubjectAndSource(subjectId: String, source: ExamSource): Flow<List<Exam>>

    @Transaction
    @Query("SELECT * FROM exams WHERE id = :examId")
    suspend fun getExamWithQuestions(examId: String): ExamWithQuestions?

    @Transaction
    @Query("SELECT * FROM exams WHERE subjectId = :subjectId ORDER BY year DESC")
    fun getExamsWithQuestions(subjectId: String): Flow<List<ExamWithQuestions>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<Exam>)

    @Delete
    suspend fun deleteExam(exam: Exam)

    @Query("DELETE FROM exams WHERE subjectId = :subjectId")
    suspend fun deleteExamsBySubject(subjectId: String)

    @Query("DELETE FROM exams")
    suspend fun deleteAll()
}
