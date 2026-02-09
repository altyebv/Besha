package com.zeros.basheer.feature.feed.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.concept.data.entity.ConceptEntity

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
    val type: String,  // Maps to VariantType enum
    val source: String,  // Maps to ContentSource enum
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