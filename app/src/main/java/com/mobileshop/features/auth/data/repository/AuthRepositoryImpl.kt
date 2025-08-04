package com.mobileshop.features.auth.data.repository

import com.mobileshop.core.data.local.TokenManager
import com.mobileshop.core.data.remote.ApiService
import com.mobileshop.features.auth.data.remote.dto.LoginRequest
import com.mobileshop.features.auth.data.remote.dto.RegisterRequest
import com.mobileshop.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

// Asegúrate de que la clase NO sea abstracta
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager // Inyectamos TokenManager
) : AuthRepository {

    override suspend fun login(loginRequest: LoginRequest): Result<Unit> { // <-- Devuelve Unit
        return try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data
                // ✅ CORRECTO: Guardamos ambos tokens
                tokenManager.saveTokens(data.accessToken, data.refreshToken)
                Result.success(Unit) // <-- Devolvemos éxito sin datos extra
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error en login"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(registerRequest: RegisterRequest): Result<Unit> {
        return try {
            val response = apiService.register(registerRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error en el registro: ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
