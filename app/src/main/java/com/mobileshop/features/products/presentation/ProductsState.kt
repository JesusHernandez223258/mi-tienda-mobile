package com.mobileshop.features.products.presentation

import com.mobileshop.features.products.domain.model.Product

data class ProductsState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val error: String? = null,
    val isProductCreated: Boolean = false
)