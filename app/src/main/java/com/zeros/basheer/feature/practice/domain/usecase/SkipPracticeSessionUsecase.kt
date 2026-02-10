package com.zeros.basheer.feature.practice.domain.usecase


import com.zeros.basheer.feature.practice.domain.repository.PracticeRepository
import javax.inject.Inject

class SkipPracticeQuestionUseCase @Inject constructor(
    private val practiceRepository: PracticeRepository
) {
    suspend operator fun invoke(sessionId: Long, questionId: String) {
        practiceRepository.skipQuestion(sessionId, questionId)
    }
}