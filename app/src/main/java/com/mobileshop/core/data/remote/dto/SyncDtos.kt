package com.mobileshop.core.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for sending a single product in sync request
 */
data class ProductSyncDto(
    val tempId: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int
)

/**
 * Wrapper for sync request body
 */
data class SyncRequest(
    val products: List<ProductSyncDto>
)

/**
 * DTO for a single sync result item
 */
data class SyncResultItemDto(
    val tempId: String?,
    // Usamos @SerializedName para mapear _id de la API a serverId
    @SerializedName("_id")
    val serverId: String?,
    val nombre: String?,
    val descripcion: String?,
    val precio: Double?,
    val stock: Int?,
    val imagenUrl: String?,
    val success: Boolean,
    val errorMessage: String? = null
)

/**
 * Data returned by the /sync endpoint
 */
data class SyncResponseDto(
    val data: SyncResultDto
)

/**
 * DTO wrapping successful and failed sync lists
 */
data class SyncResultDto(
    val successful: List<SyncResultItemDto>,
    val failed: List<SyncResultItemDto>
)