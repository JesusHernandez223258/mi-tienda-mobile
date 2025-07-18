// AuthenticateUseCase.kt
package com.mobileshop.core.domain.biometric

import androidx.fragment.app.FragmentActivity
import javax.inject.Inject

class AuthenticateUseCase @Inject constructor(
    private val biometricAuthenticator: BiometricAuthenticator
) {
    fun isBiometricAvailable(activity: FragmentActivity): Boolean =
        biometricAuthenticator.isAvailable(activity)

    suspend operator fun invoke(activity: FragmentActivity): BiometricAuthResult =
        biometricAuthenticator.authenticate(activity)
}
