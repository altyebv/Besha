package com.zeros.basheer.feature.quizbank.domain.usecase


import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import javax.inject.Inject

class CompleteQuizAttemptUseCase @Inject constructor(
    private val repository: QuizBankRepository
) {
    suspend operator fun invoke(
        attemptId: Long,
        score: Int,
        totalPoints: Int,
        timeSpentSeconds: Int
    ) {
        repository.completeQuizAttempt(
            attemptId = attemptId,
            score = score,
            totalPoints = totalPoints,
            timeSpentSeconds = timeSpentSeconds
        )
    }
}