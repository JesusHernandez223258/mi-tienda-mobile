package com.mobileshop.features.auth.domain.use_case

import com.mobileshop.features.auth.data.remote.dto.RegisterRequest
import com.mobileshop.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(registerRequest: RegisterRequest) = repository.register(registerRequest)
}