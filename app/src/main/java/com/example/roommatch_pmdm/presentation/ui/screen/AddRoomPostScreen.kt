package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.roommatch_pmdm.presentation.ui.components.ActionMenu
import com.example.roommatch_pmdm.presentation.viewmodel.AddRoomPostViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddRoomPostScreen(
    navController: NavController,
    viewModel: AddRoomPostViewModel = koinViewModel()
) {
    val roomPost by viewModel.roomPost.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // Navegar atrás cuando se guarda
    LaunchedEffect(isSaved) {
        if (isSaved) {
            viewModel.clearSaved()
            navController.popBackStack()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar publicación") },
            text = { Text("¿Publicar el anuncio de '${roomPost.title}' en ${roomPost.city}?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    viewModel.save()
                }) { Text("Publicar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(topBar = { ActionMenu("Publicar habitación", navController) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = roomPost.title,
                onValueChange = { viewModel.setTitle(it) },
                label = { Text("Título del anuncio") },
                placeholder = { Text("Ej: Habitación en Malasaña") }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = roomPost.description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Descripción") },
                minLines = 3
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = roomPost.address,
                onValueChange = { viewModel.setAddress(it) },
                label = { Text("Dirección") }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = roomPost.city,
                onValueChange = { viewModel.setCity(it) },
                label = { Text("Ciudad") }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = if (roomPost.price == 0L) "" else roomPost.price.toString(),
                onValueChange = { viewModel.setPrice(it.toLongOrNull() ?: 0L) },
                label = { Text("Precio mensual (€)") }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = if (roomPost.roommates == 0) "" else roomPost.roommates.toString(),
                onValueChange = { viewModel.setRoommates(it.toIntOrNull() ?: 0) },
                label = { Text("Número de compañeros actuales") }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = roomPost.availableFrom,
                onValueChange = { viewModel.setAvailableFrom(it) },
                label = { Text("Disponible desde") },
                placeholder = { Text("Ej: 01/06/2025") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { viewModel.reset() }) { Text("Limpiar") }
                Button(
                    onClick = { showDialog = true },
                    enabled = !isLoading && roomPost.title.isNotEmpty() && roomPost.city.isNotEmpty()
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else Text("Publicar")
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun AddRoomPostScreenPreview() {
    AddRoomPostScreen(rememberNavController())
}