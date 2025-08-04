package com.mobileshop.features.auth.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)