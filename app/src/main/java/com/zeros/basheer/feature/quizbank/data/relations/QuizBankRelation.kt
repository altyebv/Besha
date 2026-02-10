package com.zeros.basheer.feature.quizbank.data.relations


import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.zeros.basheer.feature.quizbank.data.entity.*

/**
 * Exam with its questions.
 */
data class ExamWithQuestions(
    @Embedded val exam: ExamEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ExamQuestionEntity::class,
            parentColumn = "examId",
            entityColumn = "questionId"
        )
    )
    val questions: List<QuestionEntity>
)

/**
 * Quiz attempt with all question responses.
 */
data class QuizAttemptWithResponses(
    @Embedded val attempt: QuizAttemptEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "attemptId"
    )
    val responses: List<QuestionResponseEntity>
)