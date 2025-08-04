package com.mobileshop.features.products.presentation

import com.mobileshop.features.products.domain.model.Product

data class ProductsState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false, // Para mostrar un indicador de sincronización
    val products: List<Product> = emptyList(),
    val error: String? = null,
    val isProductCreated: Boolean = false
)