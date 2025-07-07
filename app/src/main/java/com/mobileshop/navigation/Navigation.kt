package com.mobileshop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobileshop.features.login.presentation.LoginScreen
import com.mobileshop.features.products.presentation.AddProductScreen
import com.mobileshop.features.products.presentation.ProductsScreen

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PRODUCT_LIST = "product_list"
    const val ADD_PRODUCT = "add_product"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.PRODUCT_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PRODUCT_LIST) {
            ProductsScreen(
                onAddProductClick = {
                    navController.navigate(Routes.ADD_PRODUCT)
                }
            )
        }

        composable(Routes.ADD_PRODUCT) {
            AddProductScreen(
                onProductCreated = {
                    navController.popBackStack()
                }
            )
        }
    }
}