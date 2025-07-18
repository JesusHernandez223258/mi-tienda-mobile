package com.mobileshop.features.products.data.repository

import android.content.Context
import android.net.Uri
import com.mobileshop.core.common.ConnectivityObserver
import com.mobileshop.core.data.remote.ApiService
import com.mobileshop.core.data.remote.dto.ProductSyncDto
import com.mobileshop.core.data.remote.dto.SyncRequest
import com.mobileshop.features.products.data.local.ProductDao
import com.mobileshop.features.products.data.local.ProductEntity
import com.mobileshop.features.products.data.mapper.toProduct
import com.mobileshop.features.products.domain.model.Product
import com.mobileshop.features.products.domain.repository.ProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
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

    override suspend fun getProducts(): Result<List<Product>> {
        val isConnected = connectivityObserver.observe().first() == ConnectivityObserver.Status.Available

        return if (isConnected) {
            // Lógica ONLINE (la que ya tienes)
            try {
                // Sincronizar pendientes ANTES de obtener la lista fresca
                syncPendingProducts()
                val response = apiService.getProducts()
                Result.success(response.data.products.map { it.toProduct() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            // Lógica OFFLINE
            try {
                // Mapear las entidades locales al modelo de dominio para la UI
                val localProducts = productDao.getAllPendingProducts().first().map { entity ->
                    Product(
                        id = "local_${entity.localId}", // ID temporal para la UI
                        name = entity.name,
                        description = entity.description,
                        price = entity.price,
                        stock = entity.stock,
                        imageUrl = entity.imagePath // La UI puede cargar la imagen desde el archivo local
                    )
                }
                Result.success(localProducts)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getProductById(id: String): Result<Product> {
        return try {
            val response = apiService.getProductById(id)
            Result.success(response.data.toProduct())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createProduct(
        name: String,
        description: String,
        price: Double,
        stock: Int,
        imageUri: Uri?
    ): Result<Unit> {
        val isConnected = connectivityObserver.observe().first() == ConnectivityObserver.Status.Available

        return if (isConnected) {
            // ---------- ONLINE ----------
            try {
                val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
                val priceBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val stockBody = stock.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart: MultipartBody.Part? = imageUri?.let { uri ->
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bytes = inputStream.readBytes()
                        val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("imagen", "product_image.jpg", requestFile)
                    }
                }

                apiService.createProduct(
                    nombre = nameBody,
                    descripcion = descriptionBody,
                    precio = priceBody,
                    stock = stockBody,
                    imagen = imagePart
                )

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            // ---------- OFFLINE ----------
            try {
                val imagePath = imageUri?.let { saveImageLocally(it) }

                val productEntity = ProductEntity(
                    name = name,
                    description = description,
                    price = price,
                    stock = stock,
                    imagePath = imagePath,
                    isSynced = false // <- importante para identificar que falta sincronizar
                )

                productDao.insertProduct(productEntity)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateProduct(id: String, name: String, description: String, price: Double, stock: Int, imageUri: Uri?): Result<Unit> {
        // La lógica es muy similar a createProduct, pero llamando a updateProduct
        return try {
            val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val priceBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val stockBody = stock.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart: MultipartBody.Part? = imageUri?.let { uri ->
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("imagen", "product_image.jpg", requestFile)
                }
            }

            apiService.updateProduct(id, nameBody, descriptionBody, priceBody, stockBody, imagePart)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteProduct(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar producto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveImageLocally(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "product_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    override suspend fun syncPendingProducts() {
        val unsyncedList = productDao.getUnsyncedProducts()
        if (unsyncedList.isEmpty()) {
            println("No hay productos pendientes para sincronizar.")
            return
        }

        // Map local entities to DTOs
        val syncRequestDtos = unsyncedList.map { entity ->
            ProductSyncDto(
                tempId = entity.localId.toString(),
                nombre = entity.name,
                descripcion = entity.description,
                precio = entity.price,
                stock = entity.stock
            )
        }

        val syncRequest = SyncRequest(products = syncRequestDtos)

        try {
            val response = apiService.syncProducts(syncRequest)
            if (response.isSuccessful) {
                // Unwrap two layers: ApiResponse<SyncResponseDto> -> SyncResponseDto -> SyncResultDto
                val apiResponse = response.body()
                val syncResponseDto = apiResponse?.data
                val resultDto = syncResponseDto?.data

                resultDto?.let { result ->
                    // Extract successful IDs
                    val successIds = result.successful
                        .mapNotNull { it.tempId?.toIntOrNull() }
                    if (successIds.isNotEmpty()) {
                        productDao.markProductsAsSynced(successIds)
                        productDao.deleteSyncedProducts()
                    }
                    // Log failures
                    if (result.failed.isNotEmpty()) {
                        val failedIds = result.failed.mapNotNull { it.tempId }
                        println("Falló la sincronización de: $failedIds")
                    }
                } ?: run {
                    println("Respuesta syncProducts inválida: body.data.data es null")
                }
            } else {
                println("Sync request failed: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            println("Error en la sincronización: ${e.message}")
        }
    }
}
