package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey val id: String, // e.g., "geography", "history"
    val name: String, // "الجغرافيا", "Geography"
    val nameAr: String, // Arabic name
    val nameEn: String, // English name
    val path: StudentPath, // SCIENCE or LITERARY
    val isMajor: Boolean = false, // true for Biology, Computer, etc.
    val order: Int, // Display order
    val iconRes: String? = null // Icon resource name
)

enum class StudentPath {
    SCIENCE,
    LITERARY,
    COMMON // For shared subjects
}