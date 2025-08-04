package com.mobileshop.core.data.remote

import com.mobileshop.core.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: dagger.Lazy<ApiService> // Usamos Lazy para evitar inyección cíclica
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val currentToken = tokenManager.getAccessToken()
        // Si la petición que falló ya usaba el token actual, no reintentamos
        if (response.request.header("Authorization")?.endsWith(currentToken ?: "") == false) {
            return null
        }

        synchronized(this) {
            val newAccessToken = runBlocking {
                getNewToken()
            }

            return if (newAccessToken != null) {
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                // Si no se pudo refrescar, no reintentamos (se requerirá login)
                null
            }
        }
    }

    private suspend fun getNewToken(): String? {
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        return try {
            val refreshResponse = apiService.get().refreshToken(mapOf("refreshToken" to refreshToken))

            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val newAccessToken = refreshResponse.body()!!.data.accessToken
                tokenManager.saveTokens(newAccessToken, refreshToken) // Guardamos el nuevo access token con el refresh token viejo
                newAccessToken
            } else {
                // El refresh token es inválido, limpiamos todo
                tokenManager.clearTokens()
                null
            }
        } catch (e: Exception) {
            tokenManager.clearTokens()
            null
        }
    }
}