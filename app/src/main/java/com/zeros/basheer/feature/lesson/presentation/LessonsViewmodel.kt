package com.zeros.basheer.feature.lesson.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.feed.domain.usecase.GetContentVariantsUseCase
import com.zeros.basheer.feature.lesson.domain.repository.LessonRepository
import com.zeros.basheer.feature.lesson.domain.model.LessonDomain
import com.zeros.basheer.feature.progress.data.dao.LessonPartProgressDao
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import com.zeros.basheer.feature.subject.domain.model.Units
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LessonsScreenState(
    val subjectId: String = "",
    val subjectName: String = "",
    val units: List<Pair<Units, List<LessonDomain>>> = emptyList(),
    val completedLessonIds: List<String> = emptyList(),
    /**
     * Maps lessonId → nextIncompletePart index.
     * 0 = not started or part 0 is next.
     * null = lesson is fully complete (re-read mode → part 0).
     */
    val nextPartByLesson: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val repository: LessonRepository,
    private val subjectRepository: SubjectRepository,
    private val progressRepository: ProgressRepository,
    private val partProgressDao: LessonPartProgressDao,
    private val getContentVariantsUseCase: GetContentVariantsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LessonsScreenState())
    val state: StateFlow<LessonsScreenState> = _state.asStateFlow()

    fun loadLessons(subjectId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, subjectId = subjectId) }

            subjectRepository.getAllSubjects().collect { subjects ->
                val subject = subjects.find { it.id == subjectId }
                if (subject == null) {
                    _state.update { it.copy(isLoading = false) }
                    return@collect
                }

                _state.update { it.copy(subjectName = subject.nameAr) }

                combine(
                    subjectRepository.getUnitsBySubject(subject.id),
                    progressRepository.getCompletedLessons()
                ) { units, completedProgress -> units to completedProgress }
                    .collect { (units, completedProgress) ->
                        val completedIds = completedProgress.map { it.lessonId }
                        val unitsWithLessons = mutableListOf<Pair<Units, List<LessonDomain>>>()
                        val allLessonIds = mutableListOf<String>()

                        for (unit in units.sortedBy { it.order }) {
                            val lessons = repository.getLessonsByUnit(unit.id).first()
                            if (lessons.isNotEmpty()) {
                                val lessonIds = lessons.map { it.id }
                                allLessonIds += lessonIds
                                val partCounts = repository.getPartCountsForLessons(lessonIds)
                                val enriched = lessons
                                    .sortedBy { it.order }
                                    .map { lesson ->
                                        lesson.copy(partCount = partCounts[lesson.id] ?: 1)
                                    }
                                unitsWithLessons.add(unit to enriched)
                            }
                        }

                        // Resolve next incomplete part for every lesson in one batch
                        val nextPartMap = resolveNextParts(allLessonIds, completedIds)

                        _state.update {
                            it.copy(
                                units = unitsWithLessons,
                                completedLessonIds = completedIds,
                                nextPartByLesson = nextPartMap,
                                isLoading = false
                            )
                        }
                    }
            }
        }
    }

    /**
     * For each lesson, find the lowest partIndex that has NOT been completed yet.
     * If all parts are done, returns 0 (re-read from beginning).
     */
    private suspend fun resolveNextParts(
        lessonIds: List<String>,
        completedLessonIds: List<String>
    ): Map<String, Int> {
        if (lessonIds.isEmpty()) return emptyMap()

        val completedParts = partProgressDao.getCompletedPartsForLessons(lessonIds)
        val completedByLesson = completedParts.groupBy { it.lessonId }

        return lessonIds.associate { lessonId ->
            val doneIndices = completedByLesson[lessonId]?.map { it.partIndex }?.toSet() ?: emptySet()
            val totalParts = partProgressDao.getTotalPartCount(lessonId).coerceAtLeast(1)

            val nextPart = (0 until totalParts).firstOrNull { it !in doneIndices } ?: 0
            lessonId to nextPart
        }
    }
}