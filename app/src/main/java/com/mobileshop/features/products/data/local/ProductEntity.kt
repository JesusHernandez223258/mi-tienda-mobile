package com.mobileshop.features.products.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products") // Cambiamos el nombre a algo más general
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val serverId: String?, // El ID que viene del servidor (_id). Es nullable para productos nuevos creados offline.
    var name: String,
    var description: String,
    var price: Double,
    var stock: Int,
    var imagePath: String?, // Ruta local o URL remota
    var isSynced: Boolean = true, // 'false' si es nuevo o modificado y no se ha enviado
    var pendingDeletion: Boolean = false // 'true' si debe ser eliminado en la próxima sincronización
)