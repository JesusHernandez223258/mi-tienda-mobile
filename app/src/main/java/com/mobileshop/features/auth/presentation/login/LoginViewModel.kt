package com.mobileshop.features.auth.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileshop.core.data.local.TokenManager // <-- ¡IMPORTANTE!
import com.mobileshop.features.auth.data.remote.dto.LoginRequest
import com.mobileshop.features.auth.domain.use_case.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun onLoginClicked(email: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            loginUseCase(LoginRequest(email, pass))
                .onSuccess {
                    _loginState.value = LoginState.Success
                }
                .onFailure {
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