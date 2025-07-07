package com.mobileshop.core.data.remote

import com.mobileshop.core.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

// data/remote/AuthInterceptor.kt
@Singleton
class AuthInterceptor @Inject constructor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // runBlocking es aceptable aquí porque el interceptor se ejecuta en un hilo de fondo de OkHttp.
        val token = runBlocking {
            tokenManager.getToken().first()
        }

        val request = chain.request().newBuilder()
        if (token != null) {
            request.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(request.build())
    }
}