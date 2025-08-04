package com.mobileshop.features.products.data.remote.dto

// Este DTO se usa para enviar un nuevo producto al servidor
data class CreateProductRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int
)