package com.zeros.basheer.feature.subject.domain.repository


import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.subject.domain.model.Units
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Subject feature (domain layer)
 */
interface SubjectRepository {

    // Subject operations
    fun getAllSubjects(): Flow<List<Subject>>
    fun getSubjectsByPath(path: StudentPath): Flow<List<Subject>>
    suspend fun getSubjectById(subjectId: String): Subject?
    suspend fun insertSubject(subject: Subject)
    suspend fun insertSubjects(subjects: List<Subject>)
    suspend fun deleteSubject(subject: Subject)
    suspend fun deleteAllSubjects()

    // Unit operations
    fun getUnitsBySubject(subjectId: String): Flow<List<Units>>
    suspend fun getUnitById(unitId: String): Units?
    suspend fun insertUnit(units: Units)
    suspend fun insertUnits(units: List<Units>)
    suspend fun deleteUnit(units: Units)
    suspend fun deleteUnitsBySubject(subjectId: String)
}