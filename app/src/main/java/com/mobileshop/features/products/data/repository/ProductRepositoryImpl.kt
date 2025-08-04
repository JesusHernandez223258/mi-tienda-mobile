// features/products/data/repository/ProductRepositoryImpl.kt
package com.mobileshop.features.products.data.repository

import android.content.Context
import android.net.Uri
import com.mobileshop.core.common.ConnectivityObserver
import com.mobileshop.core.data.remote.ApiService
import com.mobileshop.core.data.remote.dto.ProductSyncDto // <-- Importa este DTO
import com.mobileshop.core.data.remote.dto.SyncRequest // <-- Importa este DTO
import com.mobileshop.features.products.data.local.ProductDao
import com.mobileshop.features.products.data.local.ProductEntity
import com.mobileshop.features.products.domain.model.Product
import com.mobileshop.features.products.domain.repository.ProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val productDao: ProductDao,
    private val connectivityObserver: ConnectivityObserver,
    @ApplicationContext private val context: Context
) : ProductRepository {

    // --- LECTURA (SIN CAMBIOS) ---
    override fun getProducts(): Flow<Result<List<Product>>> {
        return productDao.getAllProducts().map { entities ->
            Result.success(entities.map { it.toProduct() })
        }
    }

    override suspend fun getProductById(id: String): Result<Product> {
        return try {
            val entity = productDao.getProductByServerId(id)
            if (entity != null) {
                Result.success(entity.toProduct())
            } else {
                Result.failure(Exception("Producto no encontrado localmente"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- ESCRITURA: LÓGICA OFFLINE-FIRST ---
    override suspend fun createProduct(name: String, description: String, price: Double, stock: Int, imageUri: Uri?): Result<Unit> {
        return try {
            val imagePath = imageUri?.let { saveImageLocally(it) }
            val newProduct = ProductEntity(
                serverId = null, // Aún no tiene ID del servidor
                name = name,
                description = description,
                price = price,
                stock = stock,
                imagePath = imagePath,
                isSynced = false, // Marcado como pendiente de sincronización
                pendingDeletion = false
            )
            productDao.upsert(newProduct)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(id: String, name: String, description: String, price: Double, stock: Int, imageUri: Uri?): Result<Unit> {
        return try {
            val productToUpdate = productDao.getProductByServerId(id)
                ?: return Result.failure(Exception("Producto no encontrado para actualizar"))

            val imagePath = imageUri?.let { saveImageLocally(it) }

            productToUpdate.apply {
                this.name = name
                this.description = description
                this.price = price
                this.stock = stock
                if (imagePath != null) this.imagePath = imagePath
                this.isSynced = false // Marcado como pendiente de sincronización
            }
            productDao.upsert(productToUpdate)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            productDao.markForDeletion(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncWithRemote() {
        if (connectivityObserver.observe().first() != ConnectivityObserver.Status.Available) {
            println("Sync abortado: No hay conexión a internet.")
            return
        }

        try {
            // 1. SINCRONIZAR BORRADOS
            val pendingDeletions = productDao.getProductsPendingDeletion()
            if (pendingDeletions.isNotEmpty()) {
                pendingDeletions.forEach { entity ->
                    entity.serverId?.let { apiService.deleteProduct(it) }
                }
                // Borramos de la BD local DESPUÉS de confirmar con el servidor
                productDao.deleteByLocalIds(pendingDeletions.map { it.localId })
            }

            // 2. SINCRONIZAR CREACIONES Y ACTUALIZACIONES (UPSERTS)
            val unsyncedProducts = productDao.getUnsyncedProducts().filter { !it.pendingDeletion }
            if (unsyncedProducts.isNotEmpty()) {

                // Mapeamos a DTOs para enviar a la API
                val syncDtos = unsyncedProducts.map { entity ->
                    ProductSyncDto(
                        tempId = "local_${entity.localId}",
                        nombre = entity.name,
                        descripcion = entity.description,
                        precio = entity.price,
                        stock = entity.stock
                        // Nota: La imagen se manejará en una fase posterior o se subirá por separado.
                    )
                }

                // Hacemos la llamada a la API
                val response = apiService.syncProducts(SyncRequest(products = syncDtos))
                if (response.isSuccessful) {
                    val syncResult = response.body()!!.data.data

                    // PROCESAR RESPUESTAS EXITOSAS
                    syncResult.successful.forEach { successItem ->
                        val localId = successItem.tempId?.removePrefix("local_")?.toIntOrNull()
                        if (localId != null && successItem.serverId != null) {
                            productDao.deleteByLocalIds(listOf(localId))
                        }
                    }

                    // (Opcional) Manejar los fallos si los hubiera
                    syncResult.failed.forEach { failedItem ->
                        println("Fallo al sincronizar tempId ${failedItem.tempId}: ${failedItem.errorMessage}")
                    }
                }
            }

            val remoteProducts = apiService.getAllProductsForSync().data
            val productEntities = remoteProducts.map { dto ->
                ProductEntity(
                    serverId = dto.id,
                    name = dto.nombre,
                    description = dto.descripcion,
                    price = dto.precio,
                    stock = dto.stock,
                    imagePath = dto.imagenUrl,
                    isSynced = true, // Todo lo que viene del server está sincronizado
                    pendingDeletion = false
                )
            }
            productDao.deleteAllSynced()
            productDao.upsertAll(productEntities)

            println("Sincronización completada exitosamente.")

        } catch (e: Exception) {
            println("Error durante la sincronización: ${e.message}")
        }
    }

    private suspend fun syncLocalUpserts() {
        // Obtenemos solo los productos que han sido modificados o son nuevos
        val unsyncedProducts = productDao.getUnsyncedProducts().filter { !it.pendingDeletion }
        if (unsyncedProducts.isEmpty()) return

        unsyncedProducts.forEach { localProduct ->
            try {
                // Preparamos los datos comunes como RequestBody
                val nombreBody = localProduct.name.toRequestBody("text/plain".toMediaTypeOrNull())
                val descripcionBody = localProduct.description.toRequestBody("text/plain".toMediaTypeOrNull())
                val precioBody = localProduct.price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val stockBody = localProduct.stock.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                // Preparamos la imagen (si existe) como MultipartBody.Part
                val imagePart: MultipartBody.Part? = localProduct.imagePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val requestFile = file.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("imagen", file.name, requestFile)
                    } else {
                        null
                    }
                }

                // Decidimos si es una ACTUALIZACIÓN (PUT) o una CREACIÓN (POST)
                if (localProduct.serverId != null) {
                    // --- CORRECCIÓN AQUÍ: Pasamos TODOS los parámetros ---
                    apiService.updateProduct(
                        id = localProduct.serverId!!,
                        nombre = nombreBody,
                        descripcion = descripcionBody,
                        precio = precioBody,
                        stock = stockBody,
                        imagen = imagePart
                    )
                } else {
                    // --- CORRECCIÓN AQUÍ: Pasamos TODOS los parámetros ---
                    apiService.createProduct(
                        nombre = nombreBody,
                        descripcion = descripcionBody,
                        precio = precioBody,
                        stock = stockBody,
                        imagen = imagePart
                    )
                }
                // Si la sincronización individual es exitosa, podrías marcar este producto como sincronizado.
                // Sin embargo, es más robusto esperar a la descarga completa para asegurar la consistencia.
            } catch (e: Exception) {
                println("Fallo al sincronizar producto localId ${localProduct.localId}: ${e.message}")
            }
        }
    }

    private suspend fun syncLocalDeletions() {
        val productsToDelete = productDao.getProductsPendingDeletion()
        if (productsToDelete.isNotEmpty()) {
            productsToDelete.forEach { product ->
                try {
                    product.serverId?.let { apiService.deleteProduct(it) }
                } catch (e: Exception) {
                    println("Fallo al eliminar producto ${product.serverId} en el servidor: ${e.message}")
                }
            }
            productDao.deleteByLocalIds(productsToDelete.map { it.localId })
        }
    }

    // --- MÉTODOS DE AYUDA ---
    private fun ProductEntity.toProduct(): Product {
        return Product(
            id = this.serverId ?: "local_${this.localId}",
            name = this.name,
            description = this.description,
            price = this.price,
            stock = this.stock,
            imageUrl = this.imagePath
        )
    }

    private fun saveImageLocally(uri: Uri): String {
        // ... (tu implementación actual es correcta)
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "product_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }
}