package com.mobileshop.features.auth.data.remote.dto

// DTO para la respuesta de /login
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

// DTO para el objeto de usuario anidado
data class UserDto(
    val id: String,
    val email: String,
    val role: String
)

// DTO para la respuesta de /refresh
data class RefreshResponse(
    val accessToken: String
)