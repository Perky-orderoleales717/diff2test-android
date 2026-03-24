package com.example.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: LoginRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { current -> current.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { current -> current.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val snapshot = _uiState.value
        if (snapshot.email.isBlank() || snapshot.password.isBlank()) {
            _uiState.update { current -> current.copy(errorMessage = "Email and password are required") }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _uiState.update { current -> current.copy(isLoading = true, errorMessage = null) }
            repository.login(snapshot.email, snapshot.password)
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            isLoggedIn = true,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to login",
                        )
                    }
                }
        }
    }
}

