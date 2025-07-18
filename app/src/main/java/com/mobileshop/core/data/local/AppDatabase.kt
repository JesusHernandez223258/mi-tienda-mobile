package com.mobileshop.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mobileshop.features.products.data.local.ProductDao
import com.mobileshop.features.products.data.local.ProductEntity

@Database(entities = [ProductEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}