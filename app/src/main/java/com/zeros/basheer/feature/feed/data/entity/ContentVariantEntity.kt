package com.zeros.basheer.feature.feed.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.concept.data.entity.ConceptEntity
import com.zeros.basheer.feature.feed.domain.model.ContentSource
import com.zeros.basheer.feature.feed.domain.model.VariantType

@Entity(
    tableName = "content_variants",
    foreignKeys = [
        ForeignKey(
            entity = ConceptEntity::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("conceptId"),
        Index("type"),
        Index("source")
    ]
)
data class ContentVariantEntity(
    @PrimaryKey
    val id: String,
    val conceptId: String,
    val type: VariantType,
    val source: ContentSource,
    val contentAr: String,
    val contentEn: String? = null,
    val imageUrl: String? = null,
    val authorName: String? = null,
    val authorTitle: String? = null,
    val upvotes: Int = 0,
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false
)