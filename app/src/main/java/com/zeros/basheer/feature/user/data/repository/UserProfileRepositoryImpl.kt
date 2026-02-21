package com.zeros.basheer.feature.user.data.repository

import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.user.data.dao.UserProfileDao
import com.zeros.basheer.feature.user.data.entity.UserProfileEntity
import com.zeros.basheer.feature.user.domain.model.UserProfile
import com.zeros.basheer.feature.user.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao
) : UserProfileRepository {

    override fun getProfile(): Flow<UserProfile?> =
        dao.observeProfile().map { it?.toDomain() }

    override suspend fun getProfileOnce(): UserProfile? =
        dao.getProfile()?.toDomain()

    override suspend fun saveProfile(profile: UserProfile) {
        dao.insertOrReplace(profile.toEntity())
    }

    override suspend fun updateName(name: String) {
        dao.updateName(name.trim())
    }

    override suspend fun updatePath(path: StudentPath) {
        dao.updatePath(path)
    }

    override suspend fun updateSchoolName(schoolName: String?) {
        dao.updateSchoolName(schoolName?.trim()?.ifBlank { null })
    }

    override suspend fun clearProfile() {
        dao.deleteProfile()
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun UserProfileEntity.toDomain() = UserProfile(
        name = name,
        studentPath = studentPath,
        schoolName = schoolName,
        targetExamDate = targetExamDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun UserProfile.toEntity() = UserProfileEntity(
        id = 1,
        name = name.trim(),
        studentPath = studentPath,
        schoolName = schoolName,
        targetExamDate = targetExamDate,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}