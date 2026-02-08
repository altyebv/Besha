package com.zeros.basheer.feature.subject.domain.repository

import com.zeros.basheer.feature.subject.data.dao.SubjectDao
import com.zeros.basheer.feature.subject.data.dao.UnitDao
import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.subject.data.entity.SubjectEntity
import com.zeros.basheer.feature.subject.data.entity.UnitEntity
import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.subject.domain.model.Units
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepositoryImpl @Inject constructor(
    private val subjectDao: SubjectDao,
    private val unitDao: UnitDao
) : SubjectRepository {

    // ==================== Subject Operations ====================

    override fun getAllSubjects(): Flow<List<Subject>> =
        subjectDao.getAllSubjects().map { entities ->
            entities.map { subjectEntityToDomain(it) }
        }

    override fun getSubjectsByPath(path: StudentPath): Flow<List<Subject>> =
        subjectDao.getSubjectsByPath(path).map { entities ->
            entities.map { subjectEntityToDomain(it) }
        }

    override suspend fun getSubjectById(subjectId: String): Subject? =
        subjectDao.getSubjectById(subjectId)?.let { subjectEntityToDomain(it) }

    override suspend fun insertSubject(subject: Subject) {
        subjectDao.insertSubject(subjectDomainToEntity(subject))
    }

    override suspend fun insertSubjects(subjects: List<Subject>) {
        subjectDao.insertSubjects(subjects.map { subjectDomainToEntity(it) })
    }

    override suspend fun deleteSubject(subject: Subject) {
        subjectDao.deleteSubject(subjectDomainToEntity(subject))
    }

    override suspend fun deleteAllSubjects() {
        subjectDao.deleteAllSubjects()
    }

    // ==================== Unit Operations ====================

    override fun getUnitsBySubject(subjectId: String): Flow<List<Units>> =
        unitDao.getUnitsBySubject(subjectId).map { entities ->
            entities.map { unitEntityToDomain(it) }
        }

    override suspend fun getUnitById(unitId: String): Units? =
        unitDao.getUnitById(unitId)?.let { unitEntityToDomain(it) }

    override suspend fun insertUnit(units: Units) {
        unitDao.insertUnit(unitDomainToEntity(units))
    }

    override suspend fun insertUnits(units: List<Units>) {
        unitDao.insertUnits(units.map { unitDomainToEntity(it) })
    }

    override suspend fun deleteUnit(units: Units) {
        unitDao.deleteUnit(unitDomainToEntity(units))
    }

    override suspend fun deleteUnitsBySubject(subjectId: String) {
        unitDao.deleteUnitsBySubject(subjectId)
    }

    // ==================== Mapper Functions ====================

    // Entity -> Domain
    private fun subjectEntityToDomain(entity: SubjectEntity): Subject = Subject(
        id = entity.id,
        nameAr = entity.nameAr,
        nameEn = entity.nameEn,
        path = entity.path,
        isMajor = entity.isMajor,
        order = entity.order,
        iconRes = entity.iconRes,
        colorHex = entity.colorHex
    )

    private fun unitEntityToDomain(entity: UnitEntity): Units = Units(
        id = entity.id,
        subjectId = entity.subjectId,
        title = entity.title,
        order = entity.order,
        description = entity.description,
        estimatedHours = entity.estimatedHours
    )

    // Domain -> Entity
    private fun subjectDomainToEntity(subject: Subject): SubjectEntity = SubjectEntity(
        id = subject.id,
        nameAr = subject.nameAr,
        nameEn = subject.nameEn,
        path = subject.path,
        isMajor = subject.isMajor,
        order = subject.order,
        iconRes = subject.iconRes,
        colorHex = subject.colorHex
    )

    private fun unitDomainToEntity(units: Units): UnitEntity = UnitEntity(
        id = units.id,
        subjectId = units.subjectId,
        title = units.title,
        order = units.order,
        description = units.description,
        estimatedHours = units.estimatedHours
    )
}