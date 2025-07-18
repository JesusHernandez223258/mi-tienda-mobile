package com.mobileshop.features.login.domain.use_case

import com.mobileshop.core.data.local.TokenManager
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke() {
        tokenManager.clearToken()
    }
}