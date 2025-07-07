package com.mobileshop.features.products.domain.use_case

import com.mobileshop.features.products.domain.repository.ProductRepository
import javax.inject.Inject
import android.net.Uri

class CreateProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(name: String, description: String, price: Double, stock: Int, imageUri: Uri?) =
        repository.createProduct(name, description, price, stock, imageUri)
}