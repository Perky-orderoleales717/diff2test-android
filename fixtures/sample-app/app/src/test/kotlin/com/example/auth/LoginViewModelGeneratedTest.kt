package com.example.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelGeneratedTest {
private class FakeLoginRepository(
        private val loginResult: Result<Unit> = Result.success(Unit)
    ) : LoginRepository {
        var loginCallCount = 0
        var lastEmail: String? = null
        var lastPassword: String? = null

        override suspend fun login(email: String, password: String): Result<Unit> {
            loginCallCount++
            lastEmail = email
            lastPassword = password
            return loginResult
        }
    }

    @Test
    fun `initial state is stable`() = runTest {
        val repository = FakeLoginRepository()
        val viewModel = LoginViewModel(repository, StandardTestDispatcher(testScheduler))

        val initialState = viewModel.uiState.value
        assertEquals("", initialState.email)
        assertEquals("", initialState.password)
        assertNull(initialState.errorMessage)
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isLoggedIn)
    }

    @Test
    fun `onEmailChanged updates state on success`() = runTest {
        val repository = FakeLoginRepository()
        val viewModel = LoginViewModel(repository, StandardTestDispatcher(testScheduler))

        viewModel.onEmailChanged("  TEST@Example.COM  ")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("test@example.com", state.email)
        assertNull(state.errorMessage)
    }

    @Test
    fun `onEmailChanged exposes failure state`() = runTest {
        val repository = FakeLoginRepository()
        val viewModel = LoginViewModel(repository, StandardTestDispatcher(testScheduler))

        // First trigger an error state via login validation
        viewModel.login()
        advanceUntilIdle()
        val errorState = viewModel.uiState.value
        assertTrue(errorState.errorMessage?.contains("valid email") == true)

        // Then change email (should clear error)
        viewModel.onEmailChanged("valid@example.com")
        val clearedState = viewModel.uiState.value
        assertNull(clearedState.errorMessage)
    }

    @Test
    fun `login updates state on success`() = runTest {
        val repository = FakeLoginRepository()
        val viewModel = LoginViewModel(repository, StandardTestDispatcher(testScheduler))

        // Set valid state
        viewModel.onEmailChanged("valid@example.com")
        viewModel.onPasswordChanged("password123")

        // Trigger login
        viewModel.login()
        advanceUntilIdle()

        // Collect final state after coroutine completes
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertTrue(finalState.isLoggedIn)
        assertNull(finalState.errorMessage)
    }

    @Test
    fun `login exposes failure state`() = runTest {
        val errorMessage = "Login failed"
        val repository = FakeLoginRepository(Result.failure(Exception(errorMessage)))
        val viewModel = LoginViewModel(repository, StandardTestDispatcher(testScheduler))

        // Set valid state
        viewModel.onEmailChanged("valid@example.com")
        viewModel.onPasswordChanged("password123")

        // Trigger login
        viewModel.login()
        advanceUntilIdle()

        // Collect final state after coroutine completes
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isLoading)
        assertFalse(finalState.isLoggedIn)
        assertEquals(errorMessage, finalState.errorMessage)
    }
}