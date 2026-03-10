package com.zeros.basheer.feature.user.domain.model

enum class Gender {
    MALE,
    FEMALE;

    val displayAr: String
        get() = when (this) {
            MALE   -> "ذكر"
            FEMALE -> "أنثى"
        }
}