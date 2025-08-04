package com.mobileshop.features.auth.domain.repository

import com.mobileshop.features.auth.data.remote.dto.LoginRequest
import com.mobileshop.features.auth.data.remote.dto.RegisterRequest

interface AuthRepository {
    suspend fun login(loginRequest: LoginRequest): Result<Unit>
    suspend fun register(registerRequest: RegisterRequest): Result<Unit>
}