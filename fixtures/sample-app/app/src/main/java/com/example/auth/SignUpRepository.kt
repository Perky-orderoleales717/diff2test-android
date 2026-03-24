package com.example.auth

interface SignUpRepository {
    suspend fun isEmailAvailable(email: String): Boolean
    suspend fun createAccount(fullName: String, email: String, password: String): Result<String>
}

