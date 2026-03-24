package com.example.profile

interface ProfileRepository {
    suspend fun updateProfile(nickname: String, bio: String): Result<Unit>
}

