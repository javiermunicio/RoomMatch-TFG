package com.example.roommatch_pmdm.presentation.navigation

sealed class Screen(val route: String){
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Main : Screen("Main")
    data object AddRooms : Screen ("addRoomms")
}