package com.zeros.basheer.feature.subject.data.dao

import androidx.room.*
import com.zeros.basheer.data.models.Subject
import com.zeros.basheer.data.models.StudentPath
import com.zeros.basheer.data.relations.SubjectWithUnits
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY `order`")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE path = :path ORDER BY `order`")
    fun getSubjectsByPath(path: StudentPath): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    suspend fun getSubjectById(subjectId: String): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: Subject)

    @Transaction
    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    suspend fun getSubjectWithUnits(subjectId: String): SubjectWithUnits?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()
}
