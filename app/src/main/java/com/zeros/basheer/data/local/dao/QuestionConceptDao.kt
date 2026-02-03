package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.QuestionConcept
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionConceptDao {
    @Query("SELECT * FROM question_concepts WHERE questionId = :questionId")
    fun getByQuestionId(questionId: String): Flow<List<QuestionConcept>>

    @Query("SELECT * FROM question_concepts WHERE conceptId = :conceptId")
    fun getByConceptId(conceptId: String): Flow<List<QuestionConcept>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(questionConcept: QuestionConcept)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questionConcepts: List<QuestionConcept>)

    @Delete
    suspend fun delete(questionConcept: QuestionConcept)

    @Query("DELETE FROM question_concepts WHERE questionId = :questionId")
    suspend fun deleteByQuestionId(questionId: String)
}
