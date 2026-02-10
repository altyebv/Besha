package com.zeros.basheer.feature.practice.domain.usecase


import com.zeros.basheer.feature.practice.domain.model.PracticeQuestion
import com.zeros.basheer.feature.practice.domain.repository.PracticeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPracticeQuestionsUseCase @Inject constructor(
    private val practiceRepository: PracticeRepository
) {
    suspend fun getQuestionsForSession(sessionId: Long): List<PracticeQuestion> =
        practiceRepository.getQuestionsForSession(sessionId)

    fun getQuestionsForSessionFlow(sessionId: Long): Flow<List<PracticeQuestion>> =
        practiceRepository.getQuestionsForSessionFlow(sessionId)

    suspend fun getNextUnansweredQuestion(sessionId: Long): PracticeQuestion? =
        practiceRepository.getNextUnansweredQuestion(sessionId)
}