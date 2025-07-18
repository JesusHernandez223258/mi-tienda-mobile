package com.mobileshop.features.products.domain.use_case

import com.mobileshop.features.products.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductByIdUseCase @Inject constructor(private val repository: ProductRepository) {
    suspend operator fun invoke(id: String) = repository.getProductById(id)
}
