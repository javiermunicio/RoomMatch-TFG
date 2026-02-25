package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.roommatch_pmdm.presentation.ui.components.ActionMenu
import com.example.roommatch_pmdm.presentation.viewmodel.MainScreenViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    navController: NavController,
    mainScreenViewModel: MainScreenViewModel = koinViewModel()
) {
    val rooms by mainScreenViewModel.rooms.collectAsState()

    Scaffold (
        topBar = {
            ActionMenu("Pisos disponibles:",navController)
        }
    ){ innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(
                items = rooms,
                key = { it.direction }
            ) { room ->
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    var showDialog by remember { mutableStateOf(false) }

                    // Mostrar AlertDialog
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text(text = "Eliminar piso") },
                            text = { Text("¿Estas seguro de que quieres eleminar el piso?") },
                            confirmButton = {
                                Button(onClick = { showDialog = false }) {
                                    Text("Aceptar")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showDialog = false }) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Fila superior: checkbox + título
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { mainScreenViewModel.toggleCheck(room.direction) }
                            ) {
                                Icon(
                                    imageVector = if (room.check) Icons.Filled.Remove else Icons.Filled.Add,
                                    contentDescription = if (room.check) "Cerrar" else "Abrir"
                                )
                            }

                            Text(
                                text = room.direction,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (room.check) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Direccion: ${room.direction}")
                                    Text(text = "Precio: ${room.price}")
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Column {
                                    IconButton(
                                        onClick = {showDialog = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Borrar"
                                        )
                                    }

                                    IconButton(
                                        onClick = { }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mode,
                                            contentDescription = "Edit"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen(navController =  rememberNavController())
}
