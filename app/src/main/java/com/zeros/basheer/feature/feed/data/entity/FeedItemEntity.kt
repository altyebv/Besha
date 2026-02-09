package com.zeros.basheer.feature.feed.data.entity


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.concept.data.entity.ConceptEntity
import com.zeros.basheer.feature.subject.data.entity.SubjectEntity

@Entity(
    tableName = "feed_items",
    foreignKeys = [
        ForeignKey(
            entity = ConceptEntity::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("conceptId"),
        Index("subjectId"),
        Index("questionId"),
        Index("type")
    ]
)
data class FeedItemEntity(
    @PrimaryKey
    val id: String,
    val conceptId: String,
    val subjectId: String,
    val type: String,  // Maps to FeedItemType enum
    val contentAr: String,
    val contentEn: String? = null,
    val imageUrl: String? = null,
    val interactionType: String? = null,  // Maps to InteractionType enum
    val correctAnswer: String? = null,
    val options: String? = null,
    val explanation: String? = null,
    val questionId: String? = null,
    val priority: Int = 1,
    val order: Int = 0
)