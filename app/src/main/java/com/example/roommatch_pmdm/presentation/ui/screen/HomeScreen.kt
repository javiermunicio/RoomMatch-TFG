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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.roommatch_pmdm.presentation.navigation.Screen

@Composable
fun HomeScreen(navController: NavController) {
    val innerNavController = rememberNavController()
    val currentBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val tabs = listOf(
        Triple("Inicio", Icons.Filled.Home,       Screen.Matching.route),
        Triple("Pisos",  Icons.Filled.Search,     Screen.AddRooms.route),
        Triple("Chats",  Icons.Filled.ChatBubble, Screen.ChatList.route),
        Triple("Perfil", Icons.Filled.Person,     Screen.Profile.route)
    )

    val selectedTab = when {
        currentRoute == Screen.Matching.route -> 0
        currentRoute?.startsWith("addRooms") == true ||
                currentRoute?.startsWith("newRoomPost") == true ||
                currentRoute?.startsWith("room_post") == true ||
                currentRoute?.startsWith("edit_room") == true ||
                currentRoute?.startsWith("interested") == true -> 1
        currentRoute?.startsWith("chat") == true -> 2
        currentRoute == Screen.Profile.route -> 3
        else -> 0
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor   = Color(0xFF1E88E5)
            ) {
                tabs.forEachIndexed { index, (label, icon, _) ->
                    NavigationBarItem(
                        icon     = { Icon(icon, contentDescription = label) },
                        label    = { Text(label) },
                        selected = selectedTab == index,
                        onClick  = {
                            val route = when (index) {
                                0    -> Screen.Matching.route
                                1    -> Screen.AddRooms.route
                                2    -> Screen.ChatList.route
                                3    -> Screen.Profile.route
                                else -> Screen.Matching.route
                            }
                            innerNavController.navigate(route) {
                                popUpTo(0) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = innerNavController,
            startDestination = Screen.Matching.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Matching.route) {
                MatchingScreen()
            }
            composable(Screen.AddRooms.route) {
                RoomPostListScreen(navController = innerNavController)
            }
            composable(Screen.NewRoomPost.route) {
                AddRoomPostScreen(navController = innerNavController)
            }
            composable(Screen.RoomPostDetail.route) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                RoomPostDetailScreen(postId = postId, navController = innerNavController)
            }
            composable(Screen.EditRoomPost.route) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                EditRoomPostScreen(postId = postId, navController = innerNavController)
            }
            composable(Screen.InterestedUsersList.route) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                InterestedUsersListScreen(postId = postId, navController = innerNavController)
            }
            composable(Screen.InterestedUserProfile.route) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                InterestedUsersProfileScreen(userId = userId, navController = innerNavController)
            }
            // ── CHAT: todo con innerNavController ──────────────────────────
            composable(Screen.ChatList.route) {
                ChatListScreen(navController = innerNavController)
            }
            composable(Screen.ChatDetail.route) { backStackEntry ->
                val chatUserId = backStackEntry.arguments?.getString("chatUserId") ?: ""
                ChatDetailScreen(chatUserId = chatUserId, navController = innerNavController)
            }
            // ── PERFIL: logout navega al Login externo ─────────────────────
            composable(Screen.Profile.route) {
                ProfileScreen(navController = navController)
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}