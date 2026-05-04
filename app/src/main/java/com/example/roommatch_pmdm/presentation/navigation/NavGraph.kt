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
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.ChatDetail.route) { backStackEntry ->
            val chatUserId = backStackEntry.arguments?.getString("chatUserId") ?: ""
            ChatDetailScreen(chatUserId = chatUserId, navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.AddRooms.route) {
            RoomPostListScreen(navController = navController)
        }
        composable(Screen.NewRoomPost.route) {
            AddRoomPostScreen(navController = navController)
        }
        composable(Screen.RoomPostDetail.route) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            RoomPostDetailScreen(postId = postId, navController = navController)
        }
        composable(Screen.EditRoomPost.route) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            EditRoomPostScreen(postId = postId, navController = navController)
        }
        composable(Screen.InterestedUsersList.route) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            InterestedUsersListScreen(postId = postId, navController = navController)
        }
        composable(Screen.InterestedUserProfile.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            InterestedUsersProfileScreen(userId = userId, navController = navController)
        }
    }
}