package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.roommatch_pmdm.presentation.navigation.Screen

@Composable
fun HomeScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val innerNavController = rememberNavController()

    val tabs = listOf(
        Triple("Inicio", Icons.Filled.Home, Screen.Matching.route),
        Triple("Búsqueda", Icons.Filled.Search, Screen.Matching.route),
        Triple("Chats", Icons.Filled.ChatBubble, Screen.ChatList.route),
        Triple("Perfil", Icons.Filled.Person, Screen.Profile.route)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFF1E88E5)
            ) {
                tabs.forEachIndexed { index, (label, icon, _) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            when (index) {
                                0 -> innerNavController.navigate(Screen.Matching.route) {
                                    popUpTo(0)
                                }
                                1 -> innerNavController.navigate(Screen.Matching.route) {
                                    popUpTo(0)
                                }
                                2 -> innerNavController.navigate(Screen.ChatList.route) {
                                    popUpTo(0)
                                }
                                3 -> innerNavController.navigate(Screen.Profile.route) {
                                    popUpTo(0)
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = Screen.Matching.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Matching.route) {
                MatchingScreen()
            }
            composable(Screen.ChatList.route) {
                ChatListScreen(navController = navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}
@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController =  rememberNavController())
}
