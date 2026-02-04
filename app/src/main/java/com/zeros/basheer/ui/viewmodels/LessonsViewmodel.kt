package com.zeros.basheer.ui.screens.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.data.models.Lesson
import com.zeros.basheer.data.models.Units
import com.zeros.basheer.data.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LessonsScreenState(
    val units: List<Pair<Units, List<Lesson>>> = emptyList(),
    val completedLessonIds: Set<String> = emptySet(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val repository: LessonRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LessonsScreenState())
    val state: StateFlow<LessonsScreenState> = _state.asStateFlow()

    init {
        loadLessons()
    }

    private fun loadLessons() {
        viewModelScope.launch {
            // For now, load the first subject's lessons
            // Later you can add subject selection
            repository.getAllSubjects().collect { subjects ->
                val subject = subjects.firstOrNull()
                if (subject == null) {
                    _state.update { it.copy(isLoading = false) }
                    return@collect
                }

                // Combine units and their lessons
                repository.getUnitsBySubject(subject.id).collect { units ->
                    val unitsWithLessons = mutableListOf<Pair<Units, List<Lesson>>>()

                    for (unit in units.sortedBy { it.order }) {
                        repository.getLessonsByUnit(unit.id).first().let { lessons ->
                            if (lessons.isNotEmpty()) {
                                unitsWithLessons.add(unit to lessons.sortedBy { it.order })
                            }
                        }
                    }

                    // Load completed lessons
                    repository.getCompletedLessons().collect { completedProgress ->
                        _state.update {
                            it.copy(
                                units = unitsWithLessons,
                                completedLessonIds = completedProgress.map { p -> p.lessonId }.toSet(),
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }
}