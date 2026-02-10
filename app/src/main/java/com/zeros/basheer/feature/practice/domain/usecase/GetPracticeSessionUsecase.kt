package com.zeros.basheer.feature.practice.domain.usecase

import com.zeros.basheer.feature.practice.domain.model.PracticeSession
import com.zeros.basheer.feature.practice.domain.repository.PracticeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPracticeSessionUseCase @Inject constructor(
    private val practiceRepository: PracticeRepository
) {
    suspend fun getSession(sessionId: Long): PracticeSession? =
        practiceRepository.getSession(sessionId)

    fun getSessionFlow(sessionId: Long): Flow<PracticeSession?> =
        practiceRepository.getSessionFlow(sessionId)

    suspend fun getActiveSession(): PracticeSession? =
        practiceRepository.getActiveSession()
}