package com.example.roommatch_pmdm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.presentation.navigation.NavGraph
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.ui.theme.RoomMatchTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoomMatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authRepository: AuthRepository = koinInject()  // ← inyectar
                    val startDestination = if (authRepository.isLoggedIn) {
                        Screen.Home.route
                    } else {
                        Screen.Login.route
                    }
                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
