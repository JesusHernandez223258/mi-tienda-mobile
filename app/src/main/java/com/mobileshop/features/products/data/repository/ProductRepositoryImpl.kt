package com.mobileshop.features.products.data.repository

import android.content.Context
import android.net.Uri
import com.mobileshop.core.data.remote.ApiService
import com.mobileshop.features.products.data.mapper.toProduct
import com.mobileshop.features.products.domain.model.Product
import com.mobileshop.features.products.domain.repository.ProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context // <-- Inyectamos el Context
) : ProductRepository {

    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = apiService.getProducts()
            // Accedemos a la capa 'data' de la respuesta
            val productsDto = response.data.products
            val domainList  = productsDto.map { it.toProduct() }
            Result.success(domainList)
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
        return try {
            val nameBody        = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val priceBody       = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val stockBody       = stock.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart: MultipartBody.Part? = imageUri?.let { uri ->
                // Usamos el 'context' inyectado para abrir el stream de la imagen
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
    }
}