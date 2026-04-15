package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController

/**
 * Pantalla de Perfil - Gestión de información personal del usuario
 */
@Composable
fun ProfileScreen() {
    var isEditMode by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("Juan García") }
    var age by remember { mutableStateOf("26") }
    var location by remember { mutableStateOf("Madrid") }
    var bio by remember { mutableStateOf("Busco compañero tranquilo para compartir piso") }
    var budget by remember { mutableStateOf("600") }
    var habits by remember { mutableStateOf("No fuma, tiene mascotas") }
    var preferences by remember { mutableStateOf("Apartamento tranquilo, luz natural") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header con botón de edición
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Mi Perfil",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { isEditMode = !isEditMode }) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar perfil")
            }
        }

        // Información del perfil
        if (isEditMode) {
            // Modo edición
            EditableProfileField("Nombre", fullName) { fullName = it }
            EditableProfileField("Edad", age) { age = it }
            EditableProfileField("Ubicación", location) { location = it }
            EditableProfileField("Bio", bio) { bio = it }
            EditableProfileField("Presupuesto", budget) { budget = it }
            EditableProfileField("Hábitos", habits) { habits = it }
            EditableProfileField("Preferencias", preferences) { preferences = it }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { isEditMode = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }
        } else {
            // Modo visualización
            ProfileInfoRow("Nombre", fullName)
            ProfileInfoRow("Edad", age)
            ProfileInfoRow("Ubicación", location)
            ProfileInfoRow("Bio", bio)
            ProfileInfoRow("Presupuesto", "€$budget/mes")
            ProfileInfoRow("Hábitos", habits)
            ProfileInfoRow("Preferencias", preferences)
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun EditableProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            singleLine = true
        )
    }
}
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreenPreview()
}
