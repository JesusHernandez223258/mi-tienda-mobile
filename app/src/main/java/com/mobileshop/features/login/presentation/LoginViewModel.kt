package com.mobileshop.features.login.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileshop.core.data.local.TokenManager // <-- ¡IMPORTANTE!
import com.mobileshop.features.login.data.remote.dto.LoginRequest
import com.mobileshop.features.login.domain.use_case.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val tokenManager: TokenManager // <-- 1. Inyectamos el TokenManager
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun onLoginClicked(email: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = loginUseCase(LoginRequest(email, pass))

            // 2. Ahora 'result.onSuccess' nos da el token como un String
            result.onSuccess { token ->
                tokenManager.saveToken(token) // <-- 3. GUARDAMOS EL TOKEN
                _loginState.value = LoginState.Success
            }.onFailure {
                _loginState.value = LoginState.Error(it.message ?: "Error desconocido")
            }
        }
    }
}

sealed class LoginState {
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}