package com.mobileshop.features.products.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("SELECT * FROM pending_products WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<ProductEntity>

    @Query("SELECT * FROM pending_products")
    fun getAllPendingProducts(): Flow<List<ProductEntity>>

    @Query("UPDATE pending_products SET isSynced = 1 WHERE localId IN (:ids)")
    suspend fun markProductsAsSynced(ids: List<Int>)

    @Query("UPDATE pending_products SET isSynced = 1 WHERE localId = :productId") // Ejemplo de consulta SQL
    suspend fun markAsSynced(productId: Int) // Asegúrate que

    @Query("DELETE FROM pending_products WHERE isSynced = 1")
    suspend fun deleteSyncedProducts()
}