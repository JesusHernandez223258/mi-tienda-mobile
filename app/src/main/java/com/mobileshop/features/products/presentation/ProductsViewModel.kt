package com.mobileshop.features.products.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileshop.features.products.domain.use_case.CreateProductUseCase
import com.mobileshop.features.products.domain.use_case.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val createProductUseCase: CreateProductUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProductsState())
    val state = _state.asStateFlow()

    fun getProducts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getProductsUseCase().onSuccess { products ->
                _state.update { it.copy(isLoading = false, products = products) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    // CAMBIO: Ahora acepta Uri de imagen
    fun createProduct(
        name: String,
        description: String,
        price: Double,
        stock: Int,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            createProductUseCase(name, description, price, stock, imageUri)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isProductCreated = true) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun resetProductCreationStatus() {
        _state.update { it.copy(isProductCreated = false) }
    }
}
