package com.mobileshop.features.products.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mobileshop.features.products.domain.repository.ProductRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    // Hilt no puede inyectar repositorios directamente en Workers.
    // Se necesita un truco o, para simplificar, inyectar el DAO y ApiService
    // y llamar a una función de sincronización.
    // La forma más limpia es a través de un caso de uso.
    private val productRepository: ProductRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // La implementación real de syncPendingProducts estaría en el repo.
            (productRepository as? com.mobileshop.features.products.data.repository.ProductRepositoryImpl)?.syncPendingProducts()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}