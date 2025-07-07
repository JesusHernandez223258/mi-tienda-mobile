package com.mobileshop.features.login.data.repository

import com.mobileshop.core.data.remote.ApiService
import com.mobileshop.features.login.data.remote.dto.LoginRequest
import com.mobileshop.features.login.domain.repository.AuthRepository
import javax.inject.Inject

// Asegúrate de que la clase NO sea abstracta
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : AuthRepository {

    override suspend fun login(loginRequest: LoginRequest): Result<String>  {
        return try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()?.data?.token
                if (!token.isNullOrEmpty()) {
                    Result.success(token) // Devolvemos el token
                } else {
                    Result.failure(Exception("Token no encontrado en la respuesta de la API"))
                }
            } else {
                // Leer el mensaje de error de la API si es posible
                val errorMsg = response.errorBody()?.string() ?: "Error en el login: ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
