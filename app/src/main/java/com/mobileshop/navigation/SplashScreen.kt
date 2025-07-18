// SplashScreen.kt
package com.mobileshop.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        (context.findActivity() as? FragmentActivity)?.let {
            viewModel.startAuthentication(it)
        } ?: run {
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            AuthState.Authenticated -> navController.navigate(Routes.PRODUCT_LIST) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
            AuthState.Unauthenticated -> navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
            else -> {}
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (authState) {
            AuthState.Idle, AuthState.Loading -> CircularProgressIndicator()
            AuthState.RequiresBiometric -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Verifica tu identidad para continuar")
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    (context.findActivity() as? FragmentActivity)?.let {
                        viewModel.onBiometricAuthRequested(it)
                    }
                }) {
                    Text("Usar huella dactilar")
                }
            }
            else -> CircularProgressIndicator()
        }
    }
}

fun android.content.Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}