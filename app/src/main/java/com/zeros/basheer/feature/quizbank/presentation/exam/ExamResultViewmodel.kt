package com.zeros.basheer.feature.quizbank.presentation.exam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.quizbank.domain.model.ExamSection
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.model.QuestionResponse
import com.zeros.basheer.feature.quizbank.domain.model.QuizAttempt
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SectionResult(
    val title: String,
    val score: Int,
    val totalPoints: Int,
    val correctCount: Int,
    val totalCount: Int
)

data class ExamResultData(
    val attempt: QuizAttempt,
    val score: Int,
    val totalPoints: Int,
    val percentage: Float,
    val correctCount: Int,
    val wrongCount: Int,
    val unansweredCount: Int,
    val timeSpentSeconds: Int,
    val sectionResults: List<SectionResult>,
    val responses: List<QuestionResponse>,
    val questions: List<Question>
)

sealed class ExamResultUiState {
    object Loading : ExamResultUiState()
    data class Success(val data: ExamResultData) : ExamResultUiState()
    data class Error(val message: String) : ExamResultUiState()
}

@HiltViewModel
class ExamResultViewModel @Inject constructor(
    private val quizBankRepository: QuizBankRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val attemptId: Long = savedStateHandle.get<Long>("attemptId")
        ?: throw IllegalArgumentException("Attempt ID is required")

    private val _uiState = MutableStateFlow<ExamResultUiState>(ExamResultUiState.Loading)
    val uiState: StateFlow<ExamResultUiState> = _uiState.asStateFlow()

    init {
        loadResults()
    }

    private fun loadResults() {
        viewModelScope.launch {
            try {
                // Load attempt
                val attempt = quizBankRepository.getAttemptById(attemptId)
                    ?: throw IllegalStateException("Attempt not found")

                // Load responses
                val responses = quizBankRepository.getResponsesByAttempt(attemptId).first()

                // Load exam + questions
                val exam = quizBankRepository.getExamById(attempt.examId)
                val questions = if (exam != null) {
                    quizBankRepository.getQuestionsForExam(exam.id)
                } else emptyList()

                // Calculate stats
                val correctCount = responses.count { it.isCorrect }
                val wrongCount = responses.count { !it.isCorrect }
                val unansweredCount = questions.size - responses.size
                val score = attempt.score ?: responses.sumOf { it.pointsEarned }
                val totalPoints = attempt.totalPoints ?: exam?.totalPoints ?: 100
                val percentage = if (totalPoints > 0) (score.toFloat() / totalPoints) * 100f else 0f
                val timeSpentSeconds = attempt.timeSpentSeconds ?: 0

                // Calculate section results
                val sections = exam?.getSections() ?: emptyList()
                val sectionResults = buildSectionResults(sections, questions, responses)

                _uiState.value = ExamResultUiState.Success(
                    ExamResultData(
                        attempt = attempt,
                        score = score,
                        totalPoints = totalPoints,
                        percentage = percentage,
                        correctCount = correctCount,
                        wrongCount = wrongCount,
                        unansweredCount = unansweredCount,
                        timeSpentSeconds = timeSpentSeconds,
                        sectionResults = sectionResults,
                        responses = responses,
                        questions = questions
                    )
                )
            } catch (e: Exception) {
                _uiState.value = ExamResultUiState.Error(e.message ?: "Failed to load results")
            }
        }
    }

    private fun buildSectionResults(
        sections: List<ExamSection>,
        questions: List<Question>,
        responses: List<QuestionResponse>
    ): List<SectionResult> {
        if (sections.isEmpty()) return emptyList()

        val responseMap = responses.associateBy { it.questionId }
        val questionMap = questions.associateBy { it.id }

        return sections.map { section ->
            val sectionQuestions = section.questionIds.mapNotNull { questionMap[it] }
            val sectionResponses = section.questionIds.mapNotNull { responseMap[it] }
            val sectionScore = sectionResponses.sumOf { it.pointsEarned }
            val sectionTotal = section.points ?: sectionQuestions.sumOf { it.points }
            val correctInSection = sectionResponses.count { it.isCorrect }

            SectionResult(
                title = section.title,
                score = sectionScore,
                totalPoints = sectionTotal,
                correctCount = correctInSection,
                totalCount = sectionQuestions.size
            )
        }
    }

    fun retry() {
        _uiState.value = ExamResultUiState.Loading
        loadResults()
    }
}