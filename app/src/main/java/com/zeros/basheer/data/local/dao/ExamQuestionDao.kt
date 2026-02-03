package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.ExamQuestion
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamQuestionDao {
    @Query("SELECT * FROM exam_questions WHERE examId = :examId ORDER BY `order`")
    fun getByExamId(examId: String): Flow<List<ExamQuestion>>

    @Query("SELECT * FROM exam_questions WHERE questionId = :questionId")
    fun getByQuestionId(questionId: String): Flow<List<ExamQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(examQuestion: ExamQuestion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(examQuestions: List<ExamQuestion>)

    @Delete
    suspend fun delete(examQuestion: ExamQuestion)

    @Query("DELETE FROM exam_questions WHERE examId = :examId")
    suspend fun deleteByExamId(examId: String)
}
