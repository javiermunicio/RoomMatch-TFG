package com.example.roommatch_pmdm.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.navigation.NavController
import com.example.roommatch_pmdm.presentation.navigation.Screen


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ActionMenu(title:String, navController: NavController) {
    // Estado para controlar la visibilidad del menú
    var expanded by remember { mutableStateOf(false)

    }

    // Barra de herramientas (TopAppBar)
    TopAppBar(
        title = { Text(title)},
        actions = {
            // Botón de menú
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú"
                )
            }

            // Menú desplegable
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = {("Atrás")},
                    leadingIcon = {
                        Icon(

                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Menu"
                        )
                    },
                    onClick = {
                        // Acción 2
                        expanded = false
                        navController.popBackStack()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Tablón de Anuncios") },
                    onClick = {
                        expanded = false
                        navController.navigate(Screen.AddRooms.route) {
                            popUpTo(Screen.AddRooms.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Publicar Habitación") },
                    onClick = {
                        expanded = false
                        navController.navigate(Screen.NewRoomPost.route) {
                            launchSingleTop = true
                        }
                    }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Text("Logout")
                    },
                    onClick = {
                        // Simplemente cierra el menú desplegable
                        expanded = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }

                    }
                )
            }
        }
    )
}