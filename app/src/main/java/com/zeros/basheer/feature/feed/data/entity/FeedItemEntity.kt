package com.zeros.basheer.feature.feed.data.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_items")
data class FeedItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val contentType: String, // "lesson", "exam", "concept"
    val contentId: String,
    val timestamp: Long,
    val isCompleted: Boolean = false
)