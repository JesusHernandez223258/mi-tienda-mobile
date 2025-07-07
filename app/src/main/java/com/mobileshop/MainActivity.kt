package com.mobileshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mobileshop.navigation.AppNavigation
import com.mobileshop.ui.theme.MobileShopTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // <-- No olvides esta anotación para Hilt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() lo puedes mantener si lo necesitas
        setContent {
            MobileShopTheme {
                AppNavigation()
            }
        }
    }
}