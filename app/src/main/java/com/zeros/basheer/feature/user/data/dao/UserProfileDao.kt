package com.zeros.basheer.feature.user.data.dao

import androidx.room.*
import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.user.data.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET name = :name, updatedAt = :now WHERE id = 1")
    suspend fun updateName(name: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET studentPath = :path, updatedAt = :now WHERE id = 1")
    suspend fun updatePath(path: StudentPath, now: Long = System.currentTimeMillis())

    @Query("UPDATE user_profile SET schoolName = :schoolName, updatedAt = :now WHERE id = 1")
    suspend fun updateSchoolName(schoolName: String?, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM user_profile")
    suspend fun deleteProfile()
}