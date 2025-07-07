package com.mobileshop.features.login.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

// YA NO ES UNA ACTIVITY, ES UNA FUNCIÓN COMPOSABLE
@Composable
fun LoginScreen(
    // El ViewModel se obtiene así en Compose con Hilt
    viewModel: LoginViewModel = hiltViewModel(),
    // La acción de navegar se recibe como un parámetro
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.observeAsState()
    val context = LocalContext.current

    // Usamos LaunchedEffect para reaccionar a los cambios de estado y realizar acciones
    // como navegar o mostrar un Toast. Esto es un "efecto secundario".
    LaunchedEffect(key1 = loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                Toast.makeText(context, "Login exitoso", Toast.LENGTH_SHORT).show()
                onLoginSuccess() // <-- Llamamos a la función de navegación
            }
            is LoginState.Error -> {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
            }
            else -> Unit // No hacemos nada en Loading o estado inicial
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.onLoginClicked(email, password)
            },
            // Deshabilitamos el botón mientras se está cargando
            enabled = loginState !is LoginState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        if (loginState is LoginState.Loading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}