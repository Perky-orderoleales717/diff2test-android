package com.example.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun `submitRegistration emits validation error when input is incomplete`() = runTest(dispatcher) {
        val repository = object : SignUpRepository {
            override suspend fun isEmailAvailable(email: String): Boolean = true
            override suspend fun createAccount(fullName: String, email: String, password: String): Result<String> {
                return Result.success("user-1")
            }
        }
        val viewModel = SignUpViewModel(RegisterUserUseCase(repository), dispatcher)

        viewModel.onEmailChanged("hello@example.com")
        viewModel.submitRegistration()

        assertEquals(
            "Please enter a valid email and a password with at least 8 characters",
            viewModel.uiState.value.errorMessage,
        )
    }
}
