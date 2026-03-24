package com.example.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val repository: ProfileRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onNicknameChanged(value: String) {
        _uiState.update { current -> current.copy(nickname = value, errorMessage = null, isSaved = false) }
    }

    fun onBioChanged(value: String) {
        _uiState.update { current -> current.copy(bio = value, errorMessage = null, isSaved = false) }
    }

    fun saveProfile() {
        val snapshot = _uiState.value
        if (snapshot.nickname.length < 2) {
            _uiState.update { current -> current.copy(errorMessage = "Nickname must be at least 2 characters") }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _uiState.update { current -> current.copy(isSaving = true, errorMessage = null) }
            repository.updateProfile(snapshot.nickname, snapshot.bio)
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            isSaved = true,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Profile save failed",
                        )
                    }
                }
        }
    }
}

