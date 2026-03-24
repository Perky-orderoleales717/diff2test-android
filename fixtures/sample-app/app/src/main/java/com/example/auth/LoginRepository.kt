package com.example.auth

interface LoginRepository {
    suspend fun login(email: String, password: String): Result<Unit>
}

