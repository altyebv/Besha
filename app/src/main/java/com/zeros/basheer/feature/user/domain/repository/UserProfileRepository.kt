package com.zeros.basheer.feature.user.domain.repository


import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.user.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    /** Observe the current profile. Emits null if no profile saved yet. */
    fun getProfile(): Flow<UserProfile?>

    /** Get profile as a one-shot (for non-reactive contexts). */
    suspend fun getProfileOnce(): UserProfile?

    /** Save a new profile (used on onboarding completion). */
    suspend fun saveProfile(profile: UserProfile)

    /** Partial update — name only. */
    suspend fun updateName(name: String)

    /** Partial update — path only. */
    suspend fun updatePath(path: StudentPath)

    /** Partial update — school name. */
    suspend fun updateSchoolName(schoolName: String?)

    /** Wipes the profile row. Used for app reset. */
    suspend fun clearProfile()
}