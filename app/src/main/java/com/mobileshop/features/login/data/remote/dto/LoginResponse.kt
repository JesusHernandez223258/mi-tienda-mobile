package com.mobileshop.features.login.data.remote.dto

data class LoginResponse(
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val email: String,
    val role: String
)