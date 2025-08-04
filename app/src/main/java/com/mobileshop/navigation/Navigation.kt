// AppNavigation.kt
package com.mobileshop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobileshop.features.auth.presentation.login.LoginScreen
import com.mobileshop.features.auth.presentation.register.RegisterScreen
import com.mobileshop.features.products.presentation.AddProductScreen
import com.mobileshop.features.products.presentation.ProductsScreen
import com.mobileshop.features.products.presentation.ProductDetailScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PRODUCT_LIST = "product_list"
    const val ADD_PRODUCT = "add_product"
    const val PRODUCT_DETAIL = "product_detail"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) { SplashScreen(navController) }
        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Routes.PRODUCT_LIST) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) })
        }
        composable(Routes.PRODUCT_LIST) {
            ProductsScreen(
                navController = navController,
                onAddProductClick = { navController.navigate(Routes.ADD_PRODUCT) },
                onProductClick = { productId -> navController.navigate("${Routes.PRODUCT_DETAIL}/$productId") },
                onRegisterClick = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.ADD_PRODUCT) {
            AddProductScreen(onProductCreated = { navController.popBackStack() })
        }
        composable("${Routes.PRODUCT_DETAIL}/{productId}") {
            ProductDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() }, // Vuelve al Login
                onNavigateBack = { navController.popBackStack() } // También para la flecha de atrás
            )
        }
    }
}