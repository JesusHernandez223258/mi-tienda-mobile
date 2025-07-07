package com.mobileshop.features.products.domain.model

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String?
)