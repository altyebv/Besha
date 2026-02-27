package com.zeros.basheer.feature.user.domain.model


import com.zeros.basheer.feature.subject.data.entity.StudentPath

data class UserProfile(
    val name: String,
    val studentPath: StudentPath,
    val schoolName: String? = null,
    val targetExamDate: Long? = null,
    // New fields
    val email: String? = null,
    val state: String? = null,
    val city: String? = null,
    val major: String? = null,
    val academicTrack: String? = null,
    val dailyStudyMinutes: Int = 60,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * The subject paths this student should see.
     * Always includes their own path + COMMON subjects.
     * This is the single source of truth for subject filtering across the app.
     */
    val subjectsFilter: List<StudentPath>
        get() = listOf(studentPath, StudentPath.COMMON).distinct()
}