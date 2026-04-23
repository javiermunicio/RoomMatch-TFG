package com.example.roommatch_pmdm.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.roommatch_pmdm.presentation.ui.screen.*

// NavGraph.kt — versión corregida
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.ChatList.route) {
            ChatListScreen(navController = navController)
        }
        composable(Screen.ChatDetail.route) { backStackEntry ->
            val chatUserId = backStackEntry.arguments?.getString("chatUserId") ?: ""
            ChatDetailScreen(chatUserId = chatUserId, navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
    }
}