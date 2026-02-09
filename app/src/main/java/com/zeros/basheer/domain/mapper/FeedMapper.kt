package com.zeros.basheer.domain.mapper

import com.zeros.basheer.domain.model.FeedCard
import com.zeros.basheer.feature.feed.domain.model.FeedItem
import com.zeros.basheer.feature.subject.domain.model.Subject
import org.json.JSONArray

/**
 * Maps Room entities to UI-ready feed models.
 */
object FeedMapper {
    
    fun toFeedCard(feedItem: FeedItem, subject: Subject?): FeedCard {
        return FeedCard(
            id = feedItem.id,
            conceptId = feedItem.conceptId,
            subjectId = feedItem.subjectId,
            subjectName = subject?.nameAr ?: "",
            type = feedItem.type,
            contentAr = feedItem.contentAr,
            contentEn = feedItem.contentEn,
            imageUrl = feedItem.imageUrl,
            interactionType = feedItem.interactionType,
            correctAnswer = feedItem.correctAnswer,
            options = parseOptions(feedItem.options),
            explanation = feedItem.explanation,
            priority = feedItem.priority
        )
    }
    
    fun toFeedCards(feedItems: List<FeedItem>, subject: Subject?): List<FeedCard> {
        return feedItems.map { toFeedCard(it, subject) }
    }
    
    private fun parseOptions(optionsJson: String?): List<String>? {
        if (optionsJson.isNullOrBlank()) return null
        
        return try {
            val jsonArray = JSONArray(optionsJson)
            (0 until jsonArray.length()).map { jsonArray.getString(it) }
        } catch (e: Exception) {
            null
        }
    }
}
