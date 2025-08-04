// EN: app/src/main/java/com/mobileshop/core/data/camera/CameraManagerImpl.kt
package com.mobileshop.core.data.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.mobileshop.BuildConfig
import com.mobileshop.core.domain.camera.CameraManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManagerImpl @Inject constructor() : CameraManager { // Ya no necesita el contexto en el constructor

    override fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun createImageUri(context: Context): Uri {
        val file = File.createTempFile("camera_photo_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.provider",
            file
        )
    }
}