package com.zeros.basheer.feature.practice.domain.model


data class PracticeSession(
    val id: Long = 0,
    val subjectId: String,
    val generationType: PracticeGenerationType,
    val filterUnitIds: String? = "",
    val filterLessonIds: String? = "",
    val filterConceptIds: String? = "",
    val filterQuestionTypes: String? = "",
    val filterDifficulty: String? = null,
    val filterSource: String? = null,
    val questionCount: Int,
    val timeLimitSeconds: Int? = null,
    val shuffled: Boolean = true,
    val status: PracticeSessionStatus = PracticeSessionStatus.IN_PROGRESS,
    val currentQuestionIndex: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val skippedCount: Int = 0,
    val score: Float? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val totalTimeSeconds: Int? = null
)

data class PracticeQuestion(
    val sessionId: Long,
    val questionId: String,
    val order: Int,
    val userAnswer: String? = null,
    val isCorrect: Boolean? = null,
    val timeSpentSeconds: Int? = null,
    val answeredAt: Long? = null,
    val skipped: Boolean = false
)

enum class PracticeGenerationType {
    FULL_EXAM,
    BY_UNIT,
    BY_LESSON,
    BY_CONCEPT,
    BY_PROGRESS,
    WEAK_AREAS,
    QUICK_REVIEW,
    BY_TYPE,
    BY_SOURCE,
    CUSTOM
}

enum class PracticeSessionStatus {
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    ABANDONED
}