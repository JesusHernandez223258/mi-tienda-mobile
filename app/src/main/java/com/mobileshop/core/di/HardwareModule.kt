// EN: app/src/main/java/com/mobileshop/core/di/HardwareModule.kt
package com.mobileshop.core.di

import com.mobileshop.core.data.biometric.BiometricAuthenticatorImpl
import com.mobileshop.core.data.camera.CameraManagerImpl // <-- IMPORTAR
import com.mobileshop.core.domain.biometric.BiometricAuthenticator
import com.mobileshop.core.domain.camera.CameraManager // <-- IMPORTAR
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {

    @Binds
    @Singleton
    abstract fun bindBiometricAuthenticator(
        impl: BiometricAuthenticatorImpl
    ): BiometricAuthenticator

    // --- AÑADIR ESTO ---
    @Binds
    @Singleton
    abstract fun bindCameraManager(
        impl: CameraManagerImpl
    ): CameraManager
    // ---------------------
}