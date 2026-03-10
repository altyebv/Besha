package com.zeros.basheer.feature.user.domain.model

enum class StudentGrade {
    GRADE_2,
    GRADE_3,
    REPEATING;

    val displayAr: String
        get() = when (this) {
            GRADE_2   -> "الصف الثاني"
            GRADE_3   -> "الصف الثالث"
            REPEATING -> "إعادة"
        }
}