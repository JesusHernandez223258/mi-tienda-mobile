package com.mobileshop.features.products.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // --- Operaciones de Lectura ---
    @Query("SELECT * FROM products WHERE pendingDeletion = 0 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE serverId = :serverId")
    suspend fun getProductByServerId(serverId: String): ProductEntity?

    // --- Operaciones de Sincronización ---
    @Query("SELECT * FROM products WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE pendingDeletion = 1")
    suspend fun getProductsPendingDeletion(): List<ProductEntity>

    // --- Operaciones de Escritura ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: ProductEntity): Long

    @Query("UPDATE products SET pendingDeletion = 1, isSynced = 0 WHERE serverId = :serverId")
    suspend fun markForDeletion(serverId: String)

    @Query("DELETE FROM products WHERE localId IN (:ids)")
    suspend fun deleteByLocalIds(ids: List<Int>)

    @Query("DELETE FROM products WHERE isSynced = 1 AND pendingDeletion = 0")
    suspend fun deleteAllSynced()
}