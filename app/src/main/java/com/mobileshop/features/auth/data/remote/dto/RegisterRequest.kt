package com.mobileshop.features.auth.data.remote.dto

data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String
)