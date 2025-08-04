package com.mobileshop.core.data.remote.dto

data class GetProductsResponse(
    val products: List<ProductDto>
    // Puedes añadir aquí totalPages, etc., si los necesitas en el futuro
)