// BiometricAuthenticatorImpl.kt
package com.mobileshop.core.data.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.mobileshop.core.domain.biometric.BiometricAuthResult
import com.mobileshop.core.domain.biometric.BiometricAuthenticator
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import javax.inject.Inject

class BiometricAuthenticatorImpl @Inject constructor() : BiometricAuthenticator {

    override fun isAvailable(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    override suspend fun authenticate(activity: FragmentActivity): BiometricAuthResult {
        return suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(activity)

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verificación biométrica")
                .setDescription("Confirma tu identidad con huella")
                .setNegativeButtonText("Cancelar")
                .build()

            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (continuation.isActive) {
                            continuation.resume(BiometricAuthResult.Success)
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (continuation.isActive) {
                            if (
                                errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                            ) {
                                continuation.resume(BiometricAuthResult.UserCancelled)
                            } else {
                                continuation.resume(BiometricAuthResult.Error(errorCode, errString.toString()))
                            }
                        }
                    }

                    override fun onAuthenticationFailed() {
                        // No se interrumpe, se permite reintentar
                    }
                }
            )

            biometricPrompt.authenticate(promptInfo)
        }
    }
}
