package com.zeros.basheer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.data.models.Subject
import com.zeros.basheer.data.models.Units
import com.zeros.basheer.data.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectWithProgress(
    val subject: Subject,
    val totalLessons: Int,
    val completedLessons: Int,
    val units: List<Units>
)

data class MainScreenState(
    val subjects: List<SubjectWithProgress> = emptyList(),
    val completedLessonsCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: LessonRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Start with loading state
            _state.value = MainScreenState(isLoading = true)

            // Collect all subjects
            repository.getAllSubjects().collect { subjects ->
                val subjectsWithProgress = mutableListOf<SubjectWithProgress>()

                // For each subject, calculate its progress
                for (subject in subjects) {
                    // Get units
                    val units = repository.getUnitsBySubject(subject.id).first()

                    // Get lessons
                    val lessons = repository.getLessonsBySubject(subject.id).first()

                    // Get completed lessons
                    val completedLessons = repository.getCompletedLessons().first()

                    // Count completed lessons for this subject
                    val completedCount = completedLessons.count { progress ->
                        lessons.any { it.id == progress.lessonId }
                    }

                    subjectsWithProgress.add(
                        SubjectWithProgress(
                            subject = subject,
                            totalLessons = lessons.size,
                            completedLessons = completedCount,
                            units = units
                        )
                    )
                }

                // Get total completed lessons count
                val totalCompleted = repository.getCompletedLessonsCount().first()

                // Update state
                _state.value = MainScreenState(
                    subjects = subjectsWithProgress,
                    completedLessonsCount = totalCompleted,
                    isLoading = false
                )
            }
        }
    }

    fun refreshData() {
        loadData()
    }
}