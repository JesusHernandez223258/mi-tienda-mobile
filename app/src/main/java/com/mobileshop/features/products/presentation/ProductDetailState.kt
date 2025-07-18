package com.mobileshop.features.products.presentation

import com.mobileshop.features.products.domain.model.Product

data class ProductDetailState(
    val isLoading: Boolean = false,
    val product: Product? = null,
    val error: String? = null,
    val isUpdated: Boolean = false,
    val isDeleted: Boolean = false
)