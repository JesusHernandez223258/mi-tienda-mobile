package com.mobileshop.core.di

import com.mobileshop.features.login.data.repository.AuthRepositoryImpl
import com.mobileshop.features.login.domain.repository.AuthRepository
import com.mobileshop.features.products.data.repository.ProductRepositoryImpl
import com.mobileshop.features.products.domain.repository.ProductRepository
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
}