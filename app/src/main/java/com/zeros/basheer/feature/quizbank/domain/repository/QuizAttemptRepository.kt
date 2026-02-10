package com.zeros.basheer.feature.quizbank.domain.repository

import com.zeros.basheer.feature.quizbank.domain.model.QuizAttempt
import kotlinx.coroutines.flow.Flow

interface QuizAttemptRepository {
    fun getAttemptsBySubject(subjectId: String): Flow<List<QuizAttempt>>
}
