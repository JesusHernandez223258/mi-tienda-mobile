package com.mobileshop.core.data.remote

import com.mobileshop.core.data.remote.dto.ApiResponse
import com.mobileshop.core.data.remote.dto.GetProductsResponse
import com.mobileshop.core.data.remote.dto.ProductDto
import com.mobileshop.features.login.data.remote.dto.LoginRequest
import com.mobileshop.features.login.data.remote.dto.LoginResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // --- Auth ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    // --- Products ---
    @GET("productos")
    suspend fun getProducts(): ApiResponse<GetProductsResponse>

    @Multipart
    @POST("productos")
    suspend fun createProduct(
        @Part("nombre") nombre: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("precio") precio: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part imagen: MultipartBody.Part?
    ): ApiResponse<ProductDto>
}