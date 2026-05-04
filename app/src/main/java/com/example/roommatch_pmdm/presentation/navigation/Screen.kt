package com.example.roommatch_pmdm.presentation.navigation
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Matching : Screen("matching")
    data object ChatList : Screen("chat_list")

    data object Onboarding : Screen("onboarding")
    data object ChatDetail : Screen("chat_detail/{chatUserId}") {
        fun createRoute(chatUserId: String) = "chat_detail/$chatUserId"
    }

    data object Profile : Screen("profile")
    data object AddRooms : Screen("addRooms")
    data object NewRoomPost : Screen("newRoomPost")

    data object RoomPostDetail : Screen("room_post_detail/{postId}") {
        fun createRoute(postId: String) = "room_post_detail/$postId"
    }
}
