package com.mobileshop.core.data.remote

import com.mobileshop.core.data.remote.dto.ApiResponse
import com.mobileshop.core.data.remote.dto.GetProductsResponse
import com.mobileshop.core.data.remote.dto.ProductDto
import com.mobileshop.core.data.remote.dto.SyncRequest
import com.mobileshop.core.data.remote.dto.SyncResponseDto
import com.mobileshop.features.auth.data.remote.dto.LoginRequest
import com.mobileshop.features.auth.data.remote.dto.LoginResponse
import com.mobileshop.features.auth.data.remote.dto.RefreshResponse
import com.mobileshop.features.auth.data.remote.dto.RegisterRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // --- Auth ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body body: Map<String, String>): Response<ApiResponse<RefreshResponse>>

    // --- Products ---
    @GET("productos")
    suspend fun getProducts(): ApiResponse<GetProductsResponse>

    @GET("productos/{id}")
    suspend fun getProductById(@Path("id") id: String): ApiResponse<ProductDto>

    @Multipart
    @PUT("productos/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Part("nombre") nombre: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("precio") precio: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part imagen: MultipartBody.Part?
    ): ApiResponse<ProductDto>

    @DELETE("productos/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<Unit>

    @Multipart
    @POST("productos")
    suspend fun createProduct(
        @Part("nombre") nombre: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("precio") precio: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part imagen: MultipartBody.Part?
    ): ApiResponse<ProductDto>

    @GET("productos/all-for-sync")
    suspend fun getAllProductsForSync(): ApiResponse<List<ProductDto>>

    @POST("productos/sync")
    suspend fun syncProducts(@Body request: SyncRequest): Response<ApiResponse<SyncResponseDto>>
}