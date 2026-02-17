package com.zeros.basheer.feature.quizbank.presentation.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.quizbank.domain.model.Exam
import com.zeros.basheer.feature.quizbank.domain.model.ExamSection
import com.zeros.basheer.feature.quizbank.domain.model.ExamType
import com.zeros.basheer.feature.quizbank.domain.model.QuizAttempt
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExamEntryState(
    val exam: Exam? = null,
    val sections: List<ExamSection> = emptyList(),
    val questionCount: Int = 0,
    val lastAttempt: QuizAttempt? = null,
    val expandedSections: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed class ExamEntryEvent {
    object StartExam : ExamEntryEvent()
    object ToggleSections : ExamEntryEvent()
}

@HiltViewModel
class ExamEntryViewModel @Inject constructor(
    private val quizBankRepository: QuizBankRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val examId: String = savedStateHandle.get<String>("examId")
        ?: throw IllegalArgumentException("Exam ID is required")

    private val _state = MutableStateFlow(ExamEntryState())
    val state: StateFlow<ExamEntryState> = _state.asStateFlow()

    // Navigation events
    private val _navigateToSession = MutableStateFlow<String?>(null)
    val navigateToSession: StateFlow<String?> = _navigateToSession.asStateFlow()

    init {
        loadExam()
    }

    fun onEvent(event: ExamEntryEvent) {
        when (event) {
            ExamEntryEvent.StartExam -> _navigateToSession.value = examId
            ExamEntryEvent.ToggleSections -> _state.value =
                _state.value.copy(expandedSections = !_state.value.expandedSections)
        }
    }

    fun onNavigationHandled() {
        _navigateToSession.value = null
    }

    private fun loadExam() {
        viewModelScope.launch {
            try {
                val exam = quizBankRepository.getExamById(examId)
                    ?: throw IllegalStateException("Exam not found")

                val sections = exam.getSections()
                val questions = quizBankRepository.getQuestionsForExam(examId)
                val lastAttempt = quizBankRepository.getLastAttemptForExam(examId)

                _state.value = ExamEntryState(
                    exam = exam,
                    sections = sections,
                    questionCount = questions.size,
                    lastAttempt = lastAttempt,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to load exam",
                    isLoading = false
                )
            }
        }
    }
}