package com.example.roommatch_pmdm.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.roommatch_pmdm.presentation.ui.screen.*

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Pantalla de Login
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // Pantalla de Registro
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        // Pantalla Home (Hub principal con Bottom Navigation)
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // Pantalla de Matching
        composable(Screen.Matching.route) {
            MatchingScreen()
        }

        // Pantalla de Lista de Chats
        composable(Screen.ChatList.route) {
            ChatListScreen(navController = navController)
        }

        // Pantalla de Chat Detallado
        composable(Screen.ChatDetail.route) { backStackEntry ->
            val chatUserId = backStackEntry.arguments?.getString("chatUserId") ?: ""
            ChatDetailScreen(chatUserId = chatUserId)
        }
        composable(Screen.AddRooms.route){
            AddRoomsScreen(navController = navController)
        }

        // Pantalla de Perfil
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
    }
}
