package com.mobileshop.core.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String?
)