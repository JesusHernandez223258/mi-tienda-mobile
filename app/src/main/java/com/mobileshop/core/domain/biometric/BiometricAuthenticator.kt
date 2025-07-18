package com.mobileshop.core.domain.biometric

import androidx.fragment.app.FragmentActivity

// El resultado de la autenticación
sealed class BiometricAuthResult {
    object Success : BiometricAuthResult()
    data class Error(val code: Int, val message: String) : BiometricAuthResult()
    object Failed : BiometricAuthResult()
    object UserCancelled : BiometricAuthResult() // <-- NUEVO ESTADO
}

// La interfaz que abstrae el autenticador

interface BiometricAuthenticator {
    fun isAvailable(activity: FragmentActivity): Boolean
    suspend fun authenticate(activity: FragmentActivity): BiometricAuthResult
}