package com.zeros.basheer.feature.user.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.subject.data.entity.StudentPath

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1, // Single-user app — always row 1
    val name: String,
    val studentPath: StudentPath,
    val schoolName: String? = null,
    val targetExamDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)