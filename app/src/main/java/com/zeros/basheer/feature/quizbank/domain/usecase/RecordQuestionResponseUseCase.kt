package com.zeros.basheer.feature.quizbank.domain.usecase


import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import javax.inject.Inject

class RecordQuestionResponseUseCase @Inject constructor(
    private val repository: QuizBankRepository
) {
    suspend operator fun invoke(
        attemptId: Long,
        questionId: String,
        userAnswer: String,
        isCorrect: Boolean,
        pointsEarned: Int,
        timeSpentSeconds: Int
    ) {
        repository.recordQuestionResponse(
            attemptId = attemptId,
            questionId = questionId,
            userAnswer = userAnswer,
            isCorrect = isCorrect,
            pointsEarned = pointsEarned,
            timeSpentSeconds = timeSpentSeconds
        )
    }
}