package com.mobileshop.features.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileshop.features.auth.data.remote.dto.RegisterRequest
import com.mobileshop.features.auth.domain.use_case.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado para la UI
data class RegisterState(
    val isLoading: Boolean = false
)

sealed class RegisterEvent {
    data class Success(val message: String) : RegisterEvent()
    data class Error(val message: String) : RegisterEvent()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val registerState = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<RegisterEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onRegisterClicked(email: String, pass: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val request = RegisterRequest(email = email, password = pass, role = "admin")

            registerUseCase(request)
                .onSuccess {
                    _eventFlow.emit(RegisterEvent.Success("¡Administrador registrado!"))
                }
                .onFailure {
                    _eventFlow.emit(RegisterEvent.Error(it.message ?: "Error desconocido"))
                }
            _state.update { it.copy(isLoading = false) }
        }
    }
}