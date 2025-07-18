// SplashViewModel.kt
package com.mobileshop.navigation

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileshop.core.data.local.TokenManager
import com.mobileshop.core.domain.biometric.AuthenticateUseCase
import com.mobileshop.core.domain.biometric.BiometricAuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val authenticateUseCase: AuthenticateUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun startAuthentication(activity: FragmentActivity) {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                _authState.value = AuthState.Unauthenticated
            } else {
                if (authenticateUseCase.isBiometricAvailable(activity)) {
                    when (authenticateUseCase(activity)) {
                        is BiometricAuthResult.Success -> _authState.value = AuthState.Authenticated
                        is BiometricAuthResult.UserCancelled,
                        is BiometricAuthResult.Error -> _authState.value = AuthState.Unauthenticated
                        else -> {}
                    }
                } else {
                    _authState.value = AuthState.Authenticated
                }
            }
        }
    }

    fun onBiometricAuthRequested(activity: FragmentActivity) {
        viewModelScope.launch {
            when (authenticateUseCase(activity)) {
                is BiometricAuthResult.Success -> _authState.value = AuthState.Authenticated
                is BiometricAuthResult.UserCancelled,
                is BiometricAuthResult.Error -> _authState.value = AuthState.Unauthenticated
                is BiometricAuthResult.Failed -> {
                    // user can retry biometric
                }
            }
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object RequiresBiometric : AuthState()
}