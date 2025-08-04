package com.mobileshop.features.sync.data.repository

import com.mobileshop.core.data.local.TokenManager
import com.mobileshop.core.data.remote.ApiService
import com.mobileshop.core.data.remote.dto.ProductSyncDto
import com.mobileshop.core.data.remote.dto.SyncRequest
import com.mobileshop.features.products.data.local.ProductDao
import com.mobileshop.features.products.data.local.ProductEntity
import com.mobileshop.features.sync.domain.repository.SyncRepository
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val productDao: ProductDao,
    private val tokenManager: TokenManager
) : SyncRepository {

    override suspend fun performSync(): Result<Unit> {
        return try {
            // 1. Subir cambios locales (sin cambios aquí)
            uploadDeletions()
            uploadUpserts()

            // 2. Bajar cambios del servidor (lógica corregida)
            downloadAllProducts()

            tokenManager.saveLastSyncTimestamp(System.currentTimeMillis()) // Guardamos la fecha actual
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ... uploadDeletions y uploadUpserts se mantienen igual ...
    private suspend fun uploadDeletions() {
        val pendingDeletions = productDao.getProductsPendingDeletion()
        pendingDeletions.forEach { entity ->
            entity.serverId?.let { serverId ->
                apiService.deleteProduct(serverId)
                productDao.deleteByLocalIds(listOf(entity.localId))
            }
        }
    }

    private suspend fun uploadUpserts() {
        val unsynced = productDao.getUnsyncedProducts().filter { !it.pendingDeletion }
        if (unsynced.isEmpty()) return

        val syncDtos = unsynced.map {
            ProductSyncDto(
                tempId = "local_${it.localId}",
                nombre = it.name,
                descripcion = it.description,
                precio = it.price,
                stock = it.stock
            )
        }
        apiService.syncProducts(SyncRequest(products = syncDtos))
    }

    // ✅ CORRECCIÓN COMPLETA DE ESTA FUNCIÓN
    private suspend fun downloadAllProducts() {
        // Llamamos al endpoint que sí existe para obtener la lista completa.
        val remoteProductsResponse = apiService.getAllProductsForSync()

        // Mapeamos los DTOs remotos a Entidades de Room.
        val entities = remoteProductsResponse.data.map { dto ->
            ProductEntity(
                // Buscamos si ya existe localmente para no perder el localId
                localId = productDao.getProductByServerId(dto.id)?.localId ?: 0,
                serverId = dto.id,
                name = dto.nombre,
                description = dto.descripcion,
                price = dto.precio,
                stock = dto.stock,
                imagePath = dto.imagenUrl,
                isSynced = true, // Todos los datos que vienen del server están sincronizados
                pendingDeletion = false // Los productos activos no están pendientes de borrado
            )
        }
        // Usamos upsertAll para insertar los nuevos y actualizar los existentes en una sola operación.
        productDao.upsertAll(entities)
    }
}