package com.mobileshop.features.products.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0, // ID local autoincremental
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val imagePath: String?, // Guardamos la ruta local del archivo de imagen
    val isSynced: Boolean = false // Para saber si ya se envió a la API
)