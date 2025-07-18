// EN: app/src/main/java/com/mobileshop/core/domain/camera/CameraManager.kt
package com.mobileshop.core.domain.camera

import android.content.Context
import android.net.Uri

// Elimina la clase sellada 'CameraCaptureResult' por ahora para simplificar.
// La mantendremos en mente para futuras mejoras.

interface CameraManager {
    /**
     * Comprueba si el permiso de la cámara ha sido concedido.
     * @return 'true' si el permiso está concedido, 'false' en caso contrario.
     */
    fun hasCameraPermission(context: Context): Boolean // Pasaremos el contexto desde la UI

    /**
     * Crea una URI temporal para almacenar la foto capturada.
     * @return La [Uri] del archivo temporal.
     */
    fun createImageUri(context: Context): Uri // Pasaremos el contexto desde la UI
}