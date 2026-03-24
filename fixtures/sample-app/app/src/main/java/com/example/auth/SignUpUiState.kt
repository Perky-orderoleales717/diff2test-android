package com.example.auth

data class SignUpUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val isRegistered: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface SignUpEvent {
    data object RegistrationCompleted : SignUpEvent
}

