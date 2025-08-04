package com.mobileshop.features.products.domain.use_case

import android.net.Uri
import com.mobileshop.features.products.domain.repository.ProductRepository
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(private val repository: ProductRepository) {
    suspend operator fun invoke(id: String, name: String, description: String, price: Double, stock: Int, imageUri: Uri?) =
        repository.updateProduct(id, name, description, price, stock, imageUri)
}