package com.zeros.basheer.feature.quizbank.domain.model


/**
 * Domain model for Question (clean, no Room annotations).
 */
data class Question(
    val id: String,
    val subjectId: String,
    val unitId: String?,
    val lessonId: String?,
    /** Section this question guards as a checkpoint. Null for non-checkpoint questions. */
    val sectionId: String?,
    val type: QuestionType,
    val textAr: String,
    val textEn: String?,
    val correctAnswer: String,
    val options: String?,
    val explanation: String?,
    val imageUrl: String?,
    val tableData: String?,
    val source: QuestionSource,
    val sourceExamId: String?,
    val sourceDetails: String?,
    val sourceYear: Int?,
    val difficulty: Int,
    val cognitiveLevel: CognitiveLevel,
    val points: Int,
    val estimatedSeconds: Int,
    val feedEligible: Boolean,
    /** True when this question is an inline lesson reader gate, not a standalone practice question. */
    val isCheckpoint: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class QuestionCounts(
    val total: Int,
    val byType: Map<QuestionType, Int>,
    val bySource: Map<QuestionSource, Int>,
    val byDifficulty: Map<Int, Int>,
    val feedEligible: Int
)

enum class QuestionType {
    TRUE_FALSE,
    MCQ,
    FILL_BLANK,
    MATCH,
    SHORT_ANSWER,
    EXPLAIN,
    LIST,
    TABLE,
    FIGURE,
    COMPARE,
    ORDER
}

enum class QuestionSource {
    MINISTRY_FINAL,
    MINISTRY_SEMIFINAL,
    MINISTRY,           // alias used in some JSON files
    SCHOOL_EXAM,
    SCHOOL,             // alias used in some JSON files
    REVISION_SHEET,
    TEACHER_CONTRIB,
    ORIGINAL,
    CUSTOM,             // contributor-authored questions
    PRACTICE            // practice/drill questions
}


enum class CognitiveLevel {
    RECALL,
    UNDERSTAND,
    APPLY,
    ANALYZE
}

fun QuestionType.isFeedEligible(): Boolean {
    return this == QuestionType.TRUE_FALSE || this == QuestionType.MCQ
}