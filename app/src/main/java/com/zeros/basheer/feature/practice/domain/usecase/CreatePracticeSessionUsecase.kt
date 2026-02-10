package com.zeros.basheer.feature.practice.domain.usecase


import com.zeros.basheer.feature.practice.domain.model.PracticeGenerationType
import com.zeros.basheer.feature.practice.domain.repository.PracticeRepository
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import javax.inject.Inject

class CreatePracticeSessionUseCase @Inject constructor(
    private val practiceRepository: PracticeRepository
) {
    suspend operator fun invoke(
        subjectId: String,
        generationType: PracticeGenerationType,
        questionCount: Int = 20,
        filterUnitIds: List<String>? = null,
        filterLessonIds: List<String>? = null,
        filterConceptIds: List<String>? = null,
        filterQuestionTypes: List<QuestionType>? = null,
        filterDifficulty: IntRange? = null,
        filterSource: String? = null
    ): Long = practiceRepository.createPracticeSession(
        subjectId = subjectId,
        generationType = generationType,
        questionCount = questionCount,
        filterUnitIds = filterUnitIds,
        filterLessonIds = filterLessonIds,
        filterConceptIds = filterConceptIds,
        filterQuestionTypes = filterQuestionTypes,
        filterDifficulty = filterDifficulty,
        filterSource = filterSource
    )
}