package com.mobileshop.features.sync.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC // <-- ✅ 1. IMPORTA EL TIPO
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mobileshop.R
import com.mobileshop.features.sync.domain.use_case.SyncDataUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SyncService : Service() {

    @Inject
    lateinit var syncDataUseCase: SyncDataUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = buildNotification("Sincronizando datos...")

        // ✅ 2. ESPECIFICA EL TIPO DE SERVICIO AL INICIAR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            // Para versiones antiguas que no requieren el tipo
            startForeground(NOTIFICATION_ID, notification)
        }

        serviceScope.launch {
            syncDataUseCase().onSuccess {
                updateNotification("Sincronización completada.")
            }.onFailure {
                updateNotification("Error de sincronización.")
            }
            delay(3000)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    // ... el resto del archivo se mantiene igual ...
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MobileShop Sync")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_placeholder_loading)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notification = buildNotification(contentText).apply {
            flags = flags and Notification.FLAG_ONGOING_EVENT.inv()
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sincronización de Datos",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "sync_channel"
        const val NOTIFICATION_ID = 101
    }
}