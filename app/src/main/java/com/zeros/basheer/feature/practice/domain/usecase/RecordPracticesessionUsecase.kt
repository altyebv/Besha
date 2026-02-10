package com.zeros.basheer.feature.practice.domain.usecase


import com.zeros.basheer.feature.practice.domain.repository.PracticeRepository
import javax.inject.Inject

class RecordPracticeAnswerUseCase @Inject constructor(
    private val practiceRepository: PracticeRepository
) {
    suspend operator fun invoke(
        sessionId: Long,
        questionId: String,
        answer: String,
        isCorrect: Boolean,
        timeSeconds: Int
    ) {
        practiceRepository.recordAnswer(
            sessionId = sessionId,
            questionId = questionId,
            answer = answer,
            isCorrect = isCorrect,
            timeSeconds = timeSeconds
        )
    }
}