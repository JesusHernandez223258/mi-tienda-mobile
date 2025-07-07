package com.mobileshop.features.products.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.mobileshop.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel = hiltViewModel(),
    onAddProductClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Llama a getProducts solo una vez cuando la pantalla se compone por primera vez
    LaunchedEffect(key1 = true) {
        viewModel.getProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MobileShop") }, // Un título más de marca
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProductClick) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Producto")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.error?.let { error ->
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            // --- CAMBIO PRINCIPAL: DE LISTA A CUADRÍCULA ---
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Dos columnas, estilo Amazon/Pinterest
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.products) { product ->
                    ProductCard(product = product)
                }
            }
        }
    }
}

// --- NUEVO DISEÑO DE TARJETA DE PRODUCTO ---
@Composable
fun ProductCard(product: com.mobileshop.features.products.domain.model.Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            // --- SECCIÓN DE IMAGEN ---
            val painter = rememberAsyncImagePainter(
                model = product.imageUrl,
                // Opcional: define una imagen de carga o de error
                error = rememberAsyncImagePainter(model = R.drawable.ic_placeholder_error), // Necesitas crear este drawable
                placeholder = rememberAsyncImagePainter(model = R.drawable.ic_placeholder_loading) // Y este
            )

            Box(
                modifier = Modifier
                    .height(180.dp) // Altura fija para consistencia visual
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Muestra un indicador de carga mientras la imagen se descarga
                if (painter.state is AsyncImagePainter.State.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }

                // Muestra un icono si la imagen falla en cargar
                if (painter.state is AsyncImagePainter.State.Error) {
                    Icon(
                        imageVector = Icons.Default.ImageNotSupported,
                        contentDescription = "Error de imagen",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                }

                Image(
                    painter = painter,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop, // Recorta la imagen para llenar el espacio
                    modifier = Modifier.fillMaxSize()
                )
            }

            // --- SECCIÓN DE INFORMACIÓN ---
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Nombre del producto (máximo 2 líneas)
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis, // Añade "..." si el texto es muy largo
                    minLines = 2 // Asegura que el espacio siempre sea para 2 líneas
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Precio
                Text(
                    text = "$${"%.2f".format(product.price)}", // Formatea a 2 decimales
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stock (con un estilo más sutil)
                Text(
                    text = "Disponibles: ${product.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}