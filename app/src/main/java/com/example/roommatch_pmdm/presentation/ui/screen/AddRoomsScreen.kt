package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.roommatch_pmdm.presentation.ui.components.ActionMenu
import com.example.roommatch_pmdm.presentation.viewmodel.AddRoomsScreenViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddRoomsScreen(
    navController: NavController,
    addRoomsScreenViewModel: AddRoomsScreenViewModel = koinViewModel()
){
    val room by addRoomsScreenViewModel.room.collectAsState()
    val showDialog = remember { mutableStateOf(false) }

    Scaffold(topBar = {
        ActionMenu("Añadir Habitación", navController)
    }) {
        innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = room.buildingTipe,
                onValueChange = { addRoomsScreenViewModel.setBuildingTipe(it) },
                label = { Text("Tipo de Edificio:") }
            )
            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = room.direction,
                onValueChange = { addRoomsScreenViewModel.setDirection(it)},
                label =  {Text("Dirección:")}
            )
            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = if (room.price == 0L) "" else room.price.toString(),
                onValueChange = { text ->
                    addRoomsScreenViewModel.setPrice(text.toLongOrNull() ?: 0L)
                },
                label = { Text("Precio") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = room.roomMate,
                onValueChange = { addRoomsScreenViewModel.setRoomMate(it)},
                label =  {Text("Compañeros de piso:")}
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { addRoomsScreenViewModel.reset() }) {
                    Text("Limpiar")
                }
                Button(onClick = { showDialog.value = true }) {
                    Text("Aceptar")
                }
            }

            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text("Confirmar") },
                    text = { Text("¿Guardar el piso en:  '${room.direction}'?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDialog.value = false
                                navController.popBackStack()
                            }
                        ) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog.value = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun AddRoomPreview(){
    AddRoomsScreen(navController = rememberNavController())
}