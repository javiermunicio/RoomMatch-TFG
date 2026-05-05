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

    data object EditRoomPost : Screen("edit_room_post/{postId}") {
        fun createRoute(postId: String) = "edit_room_post/$postId"
    }

    data object InterestedUsersList : Screen("interested_users/{postId}") {
        fun createRoute(postId: String) = "interested_users/$postId"
    }

    data object InterestedUserProfile : Screen("interested_user_profile/{userId}") {
        fun createRoute(userId: String) = "interested_user_profile/$userId"
    }
}