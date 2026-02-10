package com.zeros.basheer.feature.quizbank.data.dao


import androidx.room.*
import com.zeros.basheer.feature.quizbank.data.entity.ExamQuestionEntity

//@Dao
//interface ExamQuestionDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(examQuestion: ExamQuestionEntity)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAll(examQuestions: List<ExamQuestionEntity>)
//
//    @Query("DELETE FROM exam_questions WHERE examId = :examId")
//    suspend fun deleteByExamId(examId: String)
//}