package com.zeros.basheer.feature.lesson.domain.model

data class LessonContentDomain(
    val lesson: LessonDomain,
    val sections: List<Section>
)