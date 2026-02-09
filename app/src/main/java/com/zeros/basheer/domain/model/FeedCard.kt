package com.zeros.basheer.domain.model

import com.zeros.basheer.feature.feed.domain.model.FeedItemType
import com.zeros.basheer.feature.feed.domain.model.InteractionType


/**
 * UI-ready feed card model.
 */
data class FeedCard(
    val id: String,
    val conceptId: String,
    val subjectId: String,
    val subjectName: String,
    val type: FeedItemType,
    val contentAr: String,
    val contentEn: String?,
    val imageUrl: String?,
    
    // For interactive cards
    val interactionType: InteractionType?,
    val correctAnswer: String?,
    val options: List<String>?,
    val explanation: String?,
    
    val priority: Int
)

/**
 * State of user interaction with current card.
 */
sealed class CardInteractionState {
    /** Card just displayed, waiting for user */
    object Idle : CardInteractionState()
    
    /** User is interacting (e.g., selecting MCQ option) */
    object Interacting : CardInteractionState()
    
    /** User answered, showing result */
    data class Answered(
        val userAnswer: String,
        val isCorrect: Boolean,
        val explanation: String?
    ) : CardInteractionState()
}

/**
 * Feed type badge info for UI.
 */
data class FeedTypeBadge(
    val label: String,
    val isGame: Boolean
)

fun FeedItemType.toBadge(): FeedTypeBadge {
    return when (this) {
        FeedItemType.DEFINITION -> FeedTypeBadge("تعريف", false)
        FeedItemType.FORMULA -> FeedTypeBadge("قانون", false)
        FeedItemType.DATE -> FeedTypeBadge("تاريخ", false)
        FeedItemType.FACT -> FeedTypeBadge("حقيقة", false)
        FeedItemType.RULE -> FeedTypeBadge("قاعدة", false)
        FeedItemType.TIP -> FeedTypeBadge("نصيحة", false)
        FeedItemType.MINI_QUIZ -> FeedTypeBadge("اختبر نفسك", true)
    }
}

fun InteractionType.toLabel(): String {
    return when (this) {
        InteractionType.TAP_CONFIRM -> "اضغط للمتابعة"
        InteractionType.SWIPE_TF -> "صح / خطأ"
        InteractionType.MCQ -> "اختر الإجابة"
        InteractionType.MATCH -> "وصّل"
    }
}
