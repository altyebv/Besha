package com.zeros.basheer.feature.lesson.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.progress.domain.model.UserProgress
import com.zeros.basheer.data.repository.LessonRepository
import com.zeros.basheer.domain.mapper.LessonMapper
import com.zeros.basheer.core.domain.model.Result
import com.zeros.basheer.domain.model.LessonContent
import com.zeros.basheer.feature.concept.domain.model.Concept
//import com.zeros.basheer.feature.lesson.domain.model.LessonContent
import com.zeros.basheer.feature.lesson.domain.usecase.GetLessonContentUseCase
import com.zeros.basheer.feature.lesson.domain.usecase.MarkLessonCompleteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LessonReaderState(
    val lessonContent: LessonContent? = null,
    val progress: UserProgress? = null,
    val isLoading: Boolean = true,
    val error: String? = null,

    // Concept modal
    val activeConcept: Concept? = null,

    // Progress tracking
    val readingTimeSeconds: Long = 0,
    val scrollProgress: Float = 0f,
    val hasReachedEnd: Boolean = false,

    // UI state
    val showProgressOverlay: Boolean = false
)

@HiltViewModel
class LessonReaderViewModel @Inject constructor(
    private val repository: LessonRepository,
    private val getLessonContentUseCase: GetLessonContentUseCase,
    private val markLessonCompleteUseCase: MarkLessonCompleteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _state = MutableStateFlow(LessonReaderState())
    val state: StateFlow<LessonReaderState> = _state.asStateFlow()

    private var timeTrackingJob: Job? = null
    private var isTrackingTime = false

    init {
        loadLesson()
    }
    private fun loadLesson() {
        viewModelScope.launch {
            // Use NEW use case
            when (val result = getLessonContentUseCase(lessonId)) {
                is Result.Success -> {
                    _state.update { it.copy(
                        lessonContent = result.data,
                        isLoading = false
                    )}
                }
                is Result.Error -> {
                    _state.update { it.copy(
                        error = result.message,
                        isLoading = false
                    )}
                }
            }
        }
    }
//    private fun loadLesson() {
//        viewModelScope.launch {
//            try {
//                // Load full lesson with sections and blocks
//                val lessonFull = repository.getLessonFull(lessonId)
//                if (lessonFull == null) {
//                    _state.update { it.copy(
//                        error = "الدرس غير موجود",
//                        isLoading = false
//                    )}
//                    return@launch
//                }
//
//                // Map to UI model
//                val lessonContent = LessonMapper.toLessonContent(lessonFull)
//
//                // Load progress
//                repository.getProgressByLesson(lessonId).collect { progress ->
//                    _state.update { it.copy(
//                        lessonContent = lessonContent,
//                        progress = progress,
//                        isLoading = false
//                    )}
//                }
//
//                // Update last accessed time
//                updateLastAccessed()
//            } catch (e: Exception) {
//                _state.update { it.copy(
//                    error = e.message ?: "حدث خطأ",
//                    isLoading = false
//                )}
//            }
//        }
//    }

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

    // ==================== Time Tracking ====================

    fun startTimeTracking() {
        if (isTrackingTime) return
        isTrackingTime = true

        timeTrackingJob = viewModelScope.launch {
            while (isActive && isTrackingTime) {
                delay(1000)
                _state.update { it.copy(
                    readingTimeSeconds = it.readingTimeSeconds + 1
                )}
            }
        }
    }

    fun pauseTimeTracking() {
        isTrackingTime = false
        timeTrackingJob?.cancel()
        timeTrackingJob = null

        // Save reading time to progress
        saveReadingTime()
    }

    private fun saveReadingTime() {
        viewModelScope.launch {
            val currentProgress = _state.value.progress ?: UserProgress(lessonId = lessonId)
            val totalTime = currentProgress.timeSpentSeconds + _state.value.readingTimeSeconds.toInt()
            repository.updateProgress(
                currentProgress.copy(timeSpentSeconds = totalTime)
            )
        }
    }

    // ==================== Scroll Progress ====================

    fun onScrollProgressChanged(progress: Float) {
        _state.update { it.copy(scrollProgress = progress) }

        // Check if user has reached the end (>95%)
        if (progress >= 0.95f && !_state.value.hasReachedEnd) {
            _state.update { it.copy(hasReachedEnd = true) }
        }
    }

    // ==================== Concept Modal ====================

    fun onConceptClick(conceptId: String) {
        viewModelScope.launch {
            val concept = repository.getConceptById(conceptId)
            _state.update { it.copy(activeConcept = concept) }
        }
    }

    fun dismissConceptModal() {
        _state.update { it.copy(activeConcept = null) }
    }

    // ==================== Actions ====================

    fun markAsCompleted() {
        viewModelScope.launch {
            saveReadingTime()
            markLessonCompleteUseCase(lessonId)
            // Progress and streak tracking handled by the use case
        }
    }


    fun toggleProgressOverlay() {
        _state.update { it.copy(showProgressOverlay = !it.showProgressOverlay) }
    }

    override fun onCleared() {
        super.onCleared()
        pauseTimeTracking()
    }
}