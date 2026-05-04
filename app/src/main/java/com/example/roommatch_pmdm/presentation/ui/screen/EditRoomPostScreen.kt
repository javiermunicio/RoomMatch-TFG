package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.roommatch_pmdm.presentation.viewmodel.EditRoomPostViewModel
import org.koin.androidx.compose.koinViewModel

private val RoomBlue = Color(0xFF4A90D9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoomPostScreen(
    postId: String,
    navController: NavController,
    viewModel: EditRoomPostViewModel = koinViewModel()
) {
    val roomPost        by viewModel.roomPost.collectAsState()
    val isLoading       by viewModel.isLoading.collectAsState()
    val isSaved         by viewModel.isSaved.collectAsState()
    val validationError by viewModel.validationError.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(postId) { viewModel.loadPost(postId) }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            viewModel.clearSaved()
            navController.popBackStack()
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Guardar cambios") },
            text  = { Text("¿Guardar los cambios en '${roomPost.title}'?") },
            confirmButton = {
                TextButton(onClick = { showConfirmDialog = false; viewModel.save() }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Editar anuncio", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Color.White,
                    titleContentColor = RoomBlue
                )
            )
        }
    ) { innerPadding ->

        if (isLoading && roomPost.id.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = RoomBlue) }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    modifier      = Modifier.fillMaxWidth(),
                    value         = roomPost.title,
                    onValueChange = { viewModel.setTitle(it) },
                    label         = { Text("Título del anuncio") },
                    placeholder   = { Text("Ej: Habitación en Malasaña") }
                )
                OutlinedTextField(
                    modifier      = Modifier.fillMaxWidth(),
                    value         = roomPost.description,
                    onValueChange = { viewModel.setDescription(it) },
                    label         = { Text("Descripción") },
                    minLines      = 3
                )
                OutlinedTextField(
                    modifier      = Modifier.fillMaxWidth(),
                    value         = roomPost.address,
                    onValueChange = { viewModel.setAddress(it) },
                    label         = { Text("Dirección") }
                )
                OutlinedTextField(
                    modifier      = Modifier.fillMaxWidth(),
                    value         = roomPost.city,
                    onValueChange = { viewModel.setCity(it) },
                    label         = { Text("Ciudad") }
                )
                OutlinedTextField(
                    modifier      = Modifier.fillMaxWidth(),
                    value         = if (roomPost.price == 0L) "" else roomPost.price.toString(),
                    onValueChange = { viewModel.setPrice(it.toLongOrNull() ?: 0L) },
                    label         = { Text("Precio mensual (€)") }
                )
                OutlinedTextField(
                    modifier      = Modifier.fillMaxWidth(),
                    value         = if (roomPost.roommates == 0) "" else roomPost.roommates.toString(),
                    onValueChange = { viewModel.setRoommates(it.toIntOrNull() ?: 0) },
                    label         = { Text("Número de compañeros actuales") }
                )
                OutlinedTextField(
                    modifier      = Modifier.fillMaxWidth(),
                    value         = roomPost.availableFrom,
                    onValueChange = { viewModel.setAvailableFrom(it) },
                    label         = { Text("Disponible desde") },
                    placeholder   = { Text("Ej: 01/06/2025") }
                )

                validationError?.let {
                    Text(
                        text  = it,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick  = { showConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = MaterialTheme.shapes.extraLarge,
                    colors   = ButtonDefaults.buttonColors(containerColor = RoomBlue),
                    enabled  = !isLoading && roomPost.title.isNotEmpty() && roomPost.city.isNotEmpty()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(22.dp),
                            color       = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar cambios", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}