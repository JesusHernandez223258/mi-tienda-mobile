package com.mobileshop.features.auth.domain.use_case

import com.mobileshop.features.auth.data.remote.dto.LoginRequest
import com.mobileshop.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: AuthRepository) {
    // CAMBIO: La firma ahora devuelve Result<Unit>
    suspend operator fun invoke(loginRequest: LoginRequest): Result<Unit> = repository.login(loginRequest)
}