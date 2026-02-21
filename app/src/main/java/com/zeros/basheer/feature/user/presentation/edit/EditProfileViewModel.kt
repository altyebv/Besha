package com.zeros.basheer.feature.user.presentation.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.user.domain.model.UserProfile
import com.zeros.basheer.feature.user.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val name: String = "",
    val studentPath: StudentPath? = null,
    val schoolName: String = "",
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val isLoading: Boolean = true,
    val showPathWarning: Boolean = false,
    val pendingPath: StudentPath? = null // path chosen but not confirmed yet
)

sealed interface EditProfileEvent {
    data object SavedSuccessfully : EditProfileEvent
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<EditProfileEvent>()
    val events = _events.asSharedFlow()

    private var originalPath: StudentPath? = null

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = repository.getProfileOnce()
            profile?.let {
                originalPath = it.studentPath
                _state.update { s ->
                    s.copy(
                        name = it.name,
                        studentPath = it.studentPath,
                        schoolName = it.schoolName ?: "",
                        isLoading = false
                    )
                }
            } ?: _state.update { it.copy(isLoading = false) }
        }
    }

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, nameError = null) }
    }

    fun onSchoolNameChange(value: String) {
        _state.update { it.copy(schoolName = value) }
    }

    fun onPathChangeRequested(path: StudentPath) {
        if (path == originalPath) {
            _state.update { it.copy(studentPath = path) }
            return
        }
        // Different from original — show warning first
        _state.update { it.copy(showPathWarning = true, pendingPath = path) }
    }

    fun onPathWarningConfirmed() {
        _state.update {
            it.copy(
                studentPath = it.pendingPath,
                showPathWarning = false,
                pendingPath = null
            )
        }
    }

    fun onPathWarningDismissed() {
        _state.update { it.copy(showPathWarning = false, pendingPath = null) }
    }

    fun onSave() {
        val name = _state.value.name.trim()
        if (name.isBlank() || name.length < 2) {
            _state.update { it.copy(nameError = "الرجاء إدخال اسم صحيح") }
            return
        }
        val path = _state.value.studentPath ?: return
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            repository.saveProfile(
                UserProfile(
                    name = name,
                    studentPath = path,
                    schoolName = _state.value.schoolName.trim().ifBlank { null }
                )
            )
            _state.update { it.copy(isSaving = false) }
            _events.emit(EditProfileEvent.SavedSuccessfully)
        }
    }
}