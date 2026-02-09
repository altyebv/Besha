package com.zeros.basheer.feature.feed.domain.model


data class FeedItem(
    val id: String,
    val conceptId: String,
    val subjectId: String,
    val type: FeedItemType,
    val contentAr: String,
    val contentEn: String? = null,
    val imageUrl: String? = null,
    val interactionType: InteractionType? = null,
    val correctAnswer: String? = null,
    val options: String? = null,
    val explanation: String? = null,
    val questionId: String? = null,
    val priority: Int = 1,
    val order: Int = 0
)

enum class FeedItemType {
    DEFINITION,
    FORMULA,
    DATE,
    FACT,
    RULE,
    TIP,
    MINI_QUIZ
}

enum class InteractionType {
    TAP_CONFIRM,
    SWIPE_TF,
    MCQ,
    MATCH
}

fun FeedItem.usesQuestionBank(): Boolean = questionId != null