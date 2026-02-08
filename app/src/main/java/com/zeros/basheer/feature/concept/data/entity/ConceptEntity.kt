package com.zeros.basheer.feature.concept.data.entity


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.subject.data.entity.SubjectEntity

@Entity(
    tableName = "concepts",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subjectId"),
        Index("type")
    ]
)
data class ConceptEntity(
    @PrimaryKey
    val id: String,
    val subjectId: String,
    val type: String,  // Will map to ConceptType enum
    val titleAr: String,
    val titleEn: String? = null,
    val definition: String,
    val shortDefinition: String? = null,
    val formula: String? = null,
    val imageUrl: String? = null,
    val difficulty: Int = 1,
    val extraData: String? = null
)