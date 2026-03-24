package com.example.profile

data class ProfileUiState(
    val nickname: String = "",
    val bio: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
)

