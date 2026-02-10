package com.zeros.basheer.feature.quizbank.domain.usecase


import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuestionsUseCase @Inject constructor(
    private val repository: QuizBankRepository
) {
    operator fun invoke(subjectId: String): Flow<List<Question>> =
        repository.getQuestionsBySubject(subjectId)
}