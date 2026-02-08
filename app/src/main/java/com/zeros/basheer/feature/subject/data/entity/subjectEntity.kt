package com.zeros.basheer.feature.subject.data.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey
    val id: String,                         // e.g., "geography"
    val nameAr: String,                     // "الجغرافيا"
    val nameEn: String? = null,             // "Geography"
    val path: StudentPath,                  // SCIENCE, LITERARY, COMMON
    val isMajor: Boolean = false,           // Major subject vs general
    val order: Int = 0,                     // Display order
    val iconRes: String? = null,            // Icon resource name
    val colorHex: String? = null            // Theme color: "#4CAF50"
)

enum class StudentPath {
    SCIENCE,        // العلمي
    LITERARY,       // الأدبي
    COMMON          // مشترك (for subjects like Arabic, English, Islamic Studies)
}