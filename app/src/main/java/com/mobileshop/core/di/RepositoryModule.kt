package com.mobileshop.core.di

import com.mobileshop.features.auth.data.repository.AuthRepositoryImpl
import com.mobileshop.features.auth.domain.repository.AuthRepository
import com.mobileshop.features.products.data.repository.ProductRepositoryImpl
import com.mobileshop.features.products.domain.repository.ProductRepository
import com.mobileshop.features.sync.data.repository.SyncRepositoryImpl // <-- AÑADIR ESTE IMPORT
import com.mobileshop.features.sync.domain.repository.SyncRepository // <-- AÑADIR ESTE IMPORT
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    // --- ✅ AÑADE ESTE BLOQUE PARA SOLUCIONAR EL ERROR ---
    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository
    // ----------------------------------------------------
}