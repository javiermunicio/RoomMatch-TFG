package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.RoomPostListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun RoomPostListScreen(
    navController: NavController,
    viewModel: RoomPostListViewModel = koinViewModel()
) {
    val roomPosts by viewModel.roomPosts.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddRooms.route) },
                containerColor = Color(0xFF4A90D9)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Publicar habitación", tint = Color.White)
            }
        }
    ) { innerPadding ->
        if (roomPosts.isEmpty()) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No hay anuncios disponibles", style = MaterialTheme.typography.bodyLarge)
                    Text("¡Sé el primero en publicar!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = roomPosts, key = { it.id }) { post ->
                    RoomPostCard(post = post, onDelete = { viewModel.delete(post.id) })
                }
            }
        }
    }
}

@Composable
fun RoomPostCard(post: RoomPost, onDelete: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar anuncio") },
            text = { Text("¿Eliminar '${post.title}'?") },
            confirmButton = {
                Button(onClick = { onDelete(); showDialog = false }) { Text("Eliminar") }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Color.Red)
                }
            }
            Text("📍 ${post.address}, ${post.city}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("💶 ${post.price}€/mes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            if (post.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(post.description, style = MaterialTheme.typography.bodySmall, maxLines = 3)
            }
            if (post.availableFrom.isNotEmpty()) {
                Text("Disponible desde: ${post.availableFrom}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text("Publicado por: ${post.ownerName}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}