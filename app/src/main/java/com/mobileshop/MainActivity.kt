package com.mobileshop

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mobileshop.core.common.ConnectivityObserver
import com.mobileshop.features.sync.presentation.service.SyncService
import com.mobileshop.navigation.AppNavigation
import com.mobileshop.ui.theme.MobileShopTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var connectivityObserver: ConnectivityObserver

    // Launcher para pedir permiso de notificaciones en Android 13+
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startSyncService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeNetwork()

        setContent {
            MobileShopTheme {
                AppNavigation()
            }
        }
    }

    private fun observeNetwork() {
        connectivityObserver.observe()
            .onEach { status ->
                // Cuando la red esté disponible, intenta sincronizar.
                if (status == ConnectivityObserver.Status.Available) {
                    checkPermissionsAndSync()
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun checkPermissionsAndSync() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startSyncService()
        }
    }

    private fun startSyncService() {
        val intent = Intent(this, SyncService::class.java)
        startService(intent)
    }
}