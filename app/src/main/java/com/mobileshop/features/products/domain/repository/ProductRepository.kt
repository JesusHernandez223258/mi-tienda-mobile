package com.mobileshop.features.products.domain.repository

import com.mobileshop.features.products.domain.model.Product
import android.net.Uri

interface ProductRepository {
    suspend fun getProducts(): Result<List<Product>>
    // CAMBIO: El método ahora acepta una Uri opcional
    suspend fun createProduct(
        name: String,
        description: String,
        price: Double,
        stock: Int,
        imageUri: Uri?
    ): Result<Unit>

    suspend fun getProductById(id: String): Result<Product>
    suspend fun updateProduct(id: String, name: String, description: String, price: Double, stock: Int, imageUri: Uri?): Result<Unit>
    suspend fun deleteProduct(id: String): Result<Unit>

    suspend fun syncPendingProducts()
}
