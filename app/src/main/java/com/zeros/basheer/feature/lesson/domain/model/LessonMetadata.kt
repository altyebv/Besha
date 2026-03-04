package com.zeros.basheer.feature.lesson.domain.model

/**
 * Parsed representation of the [LessonEntity.metadata] JSON blob.
 *
 * All fields are nullable — contributors only fill what's relevant.
 * The mapper returns null for the whole object if the JSON column is absent.
 *
 * @param hook        Short motivational question or statement shown before content.
 *                    e.g. "هل تساءلت يوماً كيف يحدد الملاحون موقعهم في البحر؟"
 *
 * @param orientation Bulleted "ستتعلم في هذا الدرس" list shown in the orientation strip.
 *                    e.g. ["ستفهم مفهوم خطوط الطول والعرض", "ستحسب المسافة بين نقطتين"]
 *
 * @param forwardPull One-liner shown on the exit card pulling the user toward the next lesson.
 *                    e.g. "درس واحد يفصلك عن إكمال وحدة الإحداثيات كاملة"
 */
data class LessonMetadata(
    val hook: String? = null,
    val orientation: List<String> = emptyList(),
    val forwardPull: String? = null
)