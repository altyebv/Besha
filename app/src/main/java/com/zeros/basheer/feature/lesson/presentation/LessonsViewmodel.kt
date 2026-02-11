package com.zeros.basheer.feature.lesson.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.feed.domain.usecase.GetContentVariantsUseCase
import com.zeros.basheer.feature.lesson.domain.repository.LessonRepository
import com.zeros.basheer.feature.subject.domain.model.Units
import com.zeros.basheer.feature.lesson.domain.model.LessonDomain
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LessonsScreenState(
    val units: List<Pair<Units, List<LessonDomain>>> = emptyList(),
    val completedLessonIds: Set<String> = emptySet(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val repository: LessonRepository,
    private val subjectRepository: SubjectRepository,
    private val progressRepository: ProgressRepository,
    private val getContentVariantsUseCase: GetContentVariantsUseCase,
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
            subjectRepository.getAllSubjects().collect { subjects ->
                val subject = subjects.firstOrNull()
                if (subject == null) {
                    _state.update { it.copy(isLoading = false) }
                    return@collect
                }

                // Combine units and their lessons
                subjectRepository.getUnitsBySubject(subject.id).collect { units ->
                    val unitsWithLessons = mutableListOf<Pair<Units, List<LessonDomain>>>()

                    for (unit in units.sortedBy { it.order }) {
                        val lessons = repository.getLessonsByUnit(unit.id).first()
                        if (lessons.isNotEmpty()) {
                            unitsWithLessons.add(unit to lessons.sortedBy { it.order })
                        }
                    }

                    // Load completed lessons AFTER the loop
                    progressRepository.getCompletedLessons().first().let { completedProgress ->
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