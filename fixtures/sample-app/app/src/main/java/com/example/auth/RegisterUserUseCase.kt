package com.example.auth

class RegisterUserUseCase(
    private val repository: SignUpRepository,
) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
    ): Result<String> {
        if (!repository.isEmailAvailable(email)) {
            return Result.failure(IllegalStateException("Email already exists"))
        }

        return repository.createAccount(
            fullName = fullName,
            email = email,
            password = password,
        )
    }
}

