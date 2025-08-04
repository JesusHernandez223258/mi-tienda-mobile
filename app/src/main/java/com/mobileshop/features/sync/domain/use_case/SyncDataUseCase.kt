package com.mobileshop.features.sync.domain.use_case

import com.mobileshop.features.sync.domain.repository.SyncRepository
import javax.inject.Inject

class SyncDataUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke() = syncRepository.performSync()
}