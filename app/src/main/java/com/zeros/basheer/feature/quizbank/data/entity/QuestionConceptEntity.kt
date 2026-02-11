package com.zeros.basheer.feature.quizbank.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.zeros.basheer.feature.concept.data.entity.ConceptEntity


@Entity(
    tableName = "question_concepts",
    primaryKeys = ["questionId", "conceptId"],
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ConceptEntity::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("questionId"),
        Index("conceptId")
    ]
)
data class QuestionConceptEntity(
    val questionId: String,
    val conceptId: String,
    val isPrimary: Boolean = false
)