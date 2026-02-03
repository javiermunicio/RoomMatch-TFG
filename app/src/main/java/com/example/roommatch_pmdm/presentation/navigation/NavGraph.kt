package com.example.roommatch_pmdm.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.roommatch_pmdm.presentation.ui.screen.AddRoomsScreen
import com.example.roommatch_pmdm.presentation.ui.screen.LoginScreen
import com.example.roommatch_pmdm.presentation.ui.screen.MainScreen
import com.example.roommatch_pmdm.presentation.ui.screen.RegisterScreen

@Composable
fun NavGraph( startDestination: String = Screen.Login.route){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination){
        composable(Screen.Login.route){
            LoginScreen(navController)
        }
        composable(Screen.Main.route){
            MainScreen(navController)
        }
        composable(Screen.Register.route){
            RegisterScreen(navController)
        }
        composable(Screen.AddRooms.route){
            AddRoomsScreen(navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavGraphPreview(){
    NavGraph()
}