package com.mobileshop.features.login.domain.repository

import com.mobileshop.features.login.data.remote.dto.LoginRequest

interface AuthRepository {
    suspend fun login(loginRequest: LoginRequest): Result<String>
}