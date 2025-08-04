package com.mobileshop.features.products.data.mapper

import com.mobileshop.core.data.remote.dto.ProductDto
import com.mobileshop.features.products.domain.model.Product

fun ProductDto.toProduct(): Product {
    return Product(
        id = this.id,
        name = this.nombre,
        description = this.descripcion,
        price = this.precio,
        stock = this.stock,
        imageUrl = this.imagenUrl
    )
}