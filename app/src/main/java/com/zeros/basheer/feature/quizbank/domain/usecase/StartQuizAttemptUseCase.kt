package com.zeros.basheer.feature.quizbank.domain.usecase


import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import javax.inject.Inject

class StartQuizAttemptUseCase @Inject constructor(
    private val repository: QuizBankRepository
) {
    suspend operator fun invoke(examId: String): Long =
        repository.startQuizAttempt(examId)
}