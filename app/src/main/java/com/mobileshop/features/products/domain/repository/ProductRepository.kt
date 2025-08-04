package com.mobileshop.features.products.domain.repository

import com.mobileshop.features.products.domain.model.Product
import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    // CAMBIO: Ahora devuelve un Flow para que la UI se actualice automáticamente
    fun getProducts(): Flow<Result<List<Product>>>

    suspend fun getProductById(id: String): Result<Product>
    suspend fun createProduct(name: String, description: String, price: Double, stock: Int, imageUri: Uri?): Result<Unit>
    suspend fun updateProduct(id: String, name: String, description: String, price: Double, stock: Int, imageUri: Uri?): Result<Unit>
    suspend fun deleteProduct(id: String): Result<Unit>

    // NUEVO: Método para iniciar la sincronización
    suspend fun syncWithRemote()
}