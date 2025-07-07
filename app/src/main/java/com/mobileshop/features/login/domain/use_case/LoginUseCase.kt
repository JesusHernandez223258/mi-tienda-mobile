package com.mobileshop.features.login.domain.use_case

import com.mobileshop.features.login.data.remote.dto.LoginRequest
import com.mobileshop.features.login.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(loginRequest: LoginRequest): Result<String> = repository.login(loginRequest)
}