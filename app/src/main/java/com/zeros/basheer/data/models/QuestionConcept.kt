package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table linking Questions to Concepts.
 * 
 * A question can test multiple concepts, and a concept can be tested by many questions.
 * This enables:
 *   - "Which concepts does this question test?"
 *   - "Which questions test this concept?"
 *   - Filtering questions by concept for targeted practice
 */
@Entity(
    tableName = "question_concepts",
    primaryKeys = ["questionId", "conceptId"],
    foreignKeys = [
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Concept::class,
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
data class QuestionConcept(
    val questionId: String,
    val conceptId: String,
    val isPrimary: Boolean = false      // Is this the main concept being tested?
)
