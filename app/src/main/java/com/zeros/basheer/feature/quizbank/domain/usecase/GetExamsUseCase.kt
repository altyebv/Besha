package com.zeros.basheer.feature.quizbank.domain.usecase


import com.zeros.basheer.feature.quizbank.domain.model.Exam
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExamsUseCase @Inject constructor(
    private val repository: QuizBankRepository
) {
    operator fun invoke(subjectId: String): Flow<List<Exam>> =
        repository.getExamsBySubject(subjectId)
}