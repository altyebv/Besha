package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table linking Exams to Questions.
 * 
 * Stores the order of questions within an exam and allows
 * the same question to appear in multiple exams.
 */
@Entity(
    tableName = "exam_questions",
    primaryKeys = ["examId", "questionId"],
    foreignKeys = [
        ForeignKey(
            entity = Exam::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("examId"),
        Index("questionId")
    ]
)
data class ExamQuestion(
    val examId: String,
    val questionId: String,
    val order: Int,                         // Question order within the exam
    val sectionLabel: String? = null,        // "السؤال الأول", "القسم أ", etc.
    val points: Int? = null                 // Points awarded for this question

)
