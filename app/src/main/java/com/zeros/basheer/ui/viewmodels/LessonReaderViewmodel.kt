package com.zeros.basheer.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.data.models.Lesson
import com.zeros.basheer.data.models.UserProgress
import com.zeros.basheer.data.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LessonReaderState(
    val lesson: Lesson? = null,
    val progress: UserProgress? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LessonReaderViewModel @Inject constructor(
    private val repository: LessonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _state = MutableStateFlow(LessonReaderState())
    val state: StateFlow<LessonReaderState> = _state.asStateFlow()

    init {
        loadLesson()
    }

    private fun loadLesson() {
        viewModelScope.launch {
            try {
                // Load lesson
                val lesson = repository.getLessonById(lessonId)
                if (lesson == null) {
                    _state.value = _state.value.copy(
                        error = "الدرس غير موجود",
                        isLoading = false
                    )
                    return@launch
                }

                // Load progress
                repository.getProgressByLesson(lessonId).collect { progress ->
                    _state.value = _state.value.copy(
                        lesson = lesson,
                        progress = progress,
                        isLoading = false
                    )
                }

                // Update last accessed time
                updateLastAccessed()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "حدث خطأ",
                    isLoading = false
                )
            }
        }
    }

    private fun updateLastAccessed() {
        viewModelScope.launch {
            val currentProgress = _state.value.progress
            if (currentProgress != null) {
                repository.updateProgress(
                    currentProgress.copy(lastAccessedAt = System.currentTimeMillis())
                )
            } else {
                repository.updateProgress(
                    UserProgress(
                        lessonId = lessonId,
                        lastAccessedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun markAsCompleted() {
        viewModelScope.launch {
            repository.markLessonCompleted(lessonId)
        }
    }

    fun saveNotes(notes: String) {
        viewModelScope.launch {
            val currentProgress = _state.value.progress ?: UserProgress(lessonId = lessonId)
            repository.updateProgress(
                currentProgress.copy(notes = notes)
            )
        }
    }
}