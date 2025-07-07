package com.mobileshop.core.data.remote.dto

// Un DTO genérico para las respuestas de la API
data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T
)