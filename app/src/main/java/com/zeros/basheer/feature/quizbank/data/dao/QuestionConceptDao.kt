package com.zeros.basheer.feature.quizbank.data.dao


import androidx.room.*
import com.zeros.basheer.feature.quizbank.data.entity.QuestionConceptEntity
//
//@Dao
//interface QuestionConceptDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(questionConcept: QuestionConceptEntity)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAll(questionConcepts: List<QuestionConceptEntity>)
//
//    @Query("DELETE FROM question_concepts WHERE questionId = :questionId")
//    suspend fun deleteByQuestionId(questionId: String)
//}