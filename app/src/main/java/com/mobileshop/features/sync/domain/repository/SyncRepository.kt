package com.mobileshop.features.sync.domain.repository

interface SyncRepository {
    suspend fun performSync(): Result<Unit>
}