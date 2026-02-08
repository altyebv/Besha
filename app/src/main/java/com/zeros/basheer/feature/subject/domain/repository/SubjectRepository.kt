package com.zeros.basheer.feature.subject.domain.repository


import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.subject.domain.model.Unit
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
    fun getUnitsBySubject(subjectId: String): Flow<List<Unit>>
    suspend fun getUnitById(unitId: String): Unit?
    suspend fun insertUnit(unit: Unit)
    suspend fun insertUnits(units: List<Unit>)
    suspend fun deleteUnit(unit: Unit)
    suspend fun deleteUnitsBySubject(subjectId: String)
}