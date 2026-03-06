package com.zeros.basheer.feature.quizbank.presentation.builder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.domain.repository.ContentRepository
import com.zeros.basheer.feature.practice.domain.model.PracticeGenerationType
import com.zeros.basheer.feature.practice.domain.repository.PracticeRepository
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import com.zeros.basheer.feature.subject.domain.model.Units
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PracticeBuilderState(
    // Mode selection
    val selectedMode: PracticeGenerationType = PracticeGenerationType.CUSTOM,

    // Filter options loaded from DB
    val availableUnits: List<Units> = emptyList(),
    val availableQuestionTypes: List<QuestionType> = QuestionType.entries.toList(),

    // User selections
    val selectedUnitIds: Set<String> = emptySet(),
    val selectedQuestionTypes: Set<QuestionType> = emptySet(),
    val selectedDifficultyRange: IntRange = 1..5,
    val questionCount: Int = 20,

    // Loading / error
    val isLoadingOptions: Boolean = true,
    val isCreatingSession: Boolean = false,
    val error: String? = null,

    // Subject context (passed in from QuizBank)
    val subjectId: String = "",
)

sealed class PracticeBuilderEvent {
    data class SelectMode(val mode: PracticeGenerationType) : PracticeBuilderEvent()
    data class ToggleUnit(val unitId: String) : PracticeBuilderEvent()
    data class ToggleQuestionType(val type: QuestionType) : PracticeBuilderEvent()
    data class SetDifficultyRange(val range: IntRange) : PracticeBuilderEvent()
    data class SetQuestionCount(val count: Int) : PracticeBuilderEvent()
    object StartSession : PracticeBuilderEvent()
    object ClearError : PracticeBuilderEvent()
}

@HiltViewModel
class PracticeBuilderViewModel @Inject constructor(
    private val practiceRepository: PracticeRepository,
    private val contentRepository: ContentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val subjectId: String = savedStateHandle.get<String>("subjectId") ?: ""
    private val initialMode: PracticeGenerationType = runCatching {
        PracticeGenerationType.valueOf(savedStateHandle.get<String>("mode") ?: "CUSTOM")
    }.getOrDefault(PracticeGenerationType.CUSTOM)

    private val _state = MutableStateFlow(
        PracticeBuilderState(
            subjectId = subjectId,
            selectedMode = initialMode,
        )
    )
    val state: StateFlow<PracticeBuilderState> = _state.asStateFlow()

    // One-shot: navigate to a created session
    private val _navigateToSession = MutableSharedFlow<Long>()
    val navigateToSession: SharedFlow<Long> = _navigateToSession.asSharedFlow()

    init {
        loadOptions()
    }

    fun onEvent(event: PracticeBuilderEvent) {
        when (event) {
            is PracticeBuilderEvent.SelectMode -> {
                _state.update {
                    it.copy(
                        selectedMode = event.mode,
                        // Clear incompatible selections when mode changes
                        selectedUnitIds = if (event.mode != PracticeGenerationType.BY_UNIT) emptySet() else it.selectedUnitIds,
                        selectedQuestionTypes = if (event.mode != PracticeGenerationType.BY_TYPE) emptySet() else it.selectedQuestionTypes,
                    )
                }
            }
            is PracticeBuilderEvent.ToggleUnit -> {
                _state.update {
                    val current = it.selectedUnitIds
                    it.copy(
                        selectedUnitIds = if (event.unitId in current) current - event.unitId else current + event.unitId
                    )
                }
            }
            is PracticeBuilderEvent.ToggleQuestionType -> {
                _state.update {
                    val current = it.selectedQuestionTypes
                    it.copy(
                        selectedQuestionTypes = if (event.type in current) current - event.type else current + event.type
                    )
                }
            }
            is PracticeBuilderEvent.SetDifficultyRange -> {
                _state.update { it.copy(selectedDifficultyRange = event.range) }
            }
            is PracticeBuilderEvent.SetQuestionCount -> {
                _state.update { it.copy(questionCount = event.count) }
            }
            PracticeBuilderEvent.StartSession -> startSession()
            PracticeBuilderEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadOptions() {
        viewModelScope.launch {
            contentRepository.getUnitsBySubject(subjectId)
                .catch { /* gracefully show empty list */ }
                .collect { units ->
                    _state.update { it.copy(availableUnits = units, isLoadingOptions = false) }
                }
        }
    }

    private fun startSession() {
        viewModelScope.launch {
            val s = _state.value
            _state.update { it.copy(isCreatingSession = true, error = null) }

            try {
                val unitIds = s.selectedUnitIds.takeIf { it.isNotEmpty() }?.toList()
                val types = s.selectedQuestionTypes.takeIf { it.isNotEmpty() }?.toList()
                val diffRange = s.selectedDifficultyRange.takeIf { it != 1..5 }

                val sessionId = practiceRepository.createPracticeSession(
                    subjectId = s.subjectId,
                    generationType = s.selectedMode,
                    questionCount = s.questionCount,
                    filterUnitIds = unitIds,
                    filterQuestionTypes = types,
                    filterDifficulty = diffRange,
                )
                _navigateToSession.emit(sessionId)
            } catch (e: Exception) {
                _state.update { it.copy(error = "تعذّر إنشاء الجلسة — ${e.message}") }
            } finally {
                _state.update { it.copy(isCreatingSession = false) }
            }
        }
    }
}