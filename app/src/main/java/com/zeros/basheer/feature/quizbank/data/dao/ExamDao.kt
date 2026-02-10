package com.zeros.basheer.feature.quizbank.data.dao
//
//
//import androidx.room.*
//import com.zeros.basheer.feature.quizbank.data.entity.ExamEntity
//import kotlinx.coroutines.flow.Flow
//
//@Dao
//interface ExamDao {
//    @Query("SELECT * FROM exams")
//    fun getAllExams(): Flow<List<ExamEntity>>
//
//    @Query("SELECT * FROM exams WHERE subjectId = :subjectId")
//    fun getExamsBySubject(subjectId: String): Flow<List<ExamEntity>>
//
//    @Query("SELECT * FROM exams WHERE subjectId = :subjectId AND source = :source")
//    fun getExamsBySubjectAndSource(subjectId: String, source: String): Flow<List<ExamEntity>>
//
//    @Query("SELECT * FROM exams WHERE id = :examId")
//    suspend fun getExamById(examId: String): ExamEntity?
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertExam(exam: ExamEntity)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertExams(exams: List<ExamEntity>)
//
//    @Query("DELETE FROM exams WHERE subjectId = :subjectId")
//    suspend fun deleteExamsBySubject(subjectId: String)
//}