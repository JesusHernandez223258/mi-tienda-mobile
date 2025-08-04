package com.mobileshop.features.products.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileshop.core.domain.camera.CameraManager
import com.mobileshop.features.auth.domain.use_case.LogoutUseCase
import com.mobileshop.features.products.domain.repository.ProductRepository // Importa la interfaz
import com.mobileshop.features.products.domain.use_case.* // Importa todos los casos de uso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository, // Inyectamos el Repositorio
    private val createProductUseCase: CreateProductUseCase, // Lo mantenemos para crear
    private val logoutUseCase: LogoutUseCase,
    private val cameraManager: CameraManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProductsState())
    val state = _state.asStateFlow()

    private val _uiEvent = Channel<UIEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        observeProducts()
        syncData()
    }

    private fun observeProducts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            productRepository.getProducts().collect { result ->
                result.onSuccess { products ->
                    _state.update { it.copy(isLoading = false, products = products, error = null) }
                }.onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
            }
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true) } // Opcional: mostrar un indicador de sync
            productRepository.syncWithRemote()
            _state.update { it.copy(isSyncing = false) }
        }
    }

    fun createProduct(name: String, description: String, price: Double, stock: Int, imageUri: Uri?) {
        viewModelScope.launch {
            createProductUseCase(name, description, price, stock, imageUri)
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
                .onSuccess {
                    _state.update { it.copy(isProductCreated = true) } // Para la navegación
                    syncData() // Intentamos sincronizar inmediatamente después de crear
                }
        }
    }

    // ... el resto de tus funciones (hasCameraPermission, onLogout, etc.) se mantienen igual
    fun hasCameraPermission(context: Context): Boolean = cameraManager.hasCameraPermission(context)
    fun createImageUri(context: Context): Uri = cameraManager.createImageUri(context)
    fun resetProductCreationStatus() = _state.update { it.copy(isProductCreated = false) }

    fun onLogout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiEvent.send(UIEvent.NavigateToLogin)
        }
    }

    sealed class UIEvent {
        object NavigateToLogin : UIEvent()
    }
}