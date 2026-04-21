package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

// ── Colores de la app ────────────────────────────────────────────────────────
private val RoomBlue  = Color(0xFF4A90D9)
private val RoomRed   = Color(0xFFF26B6B)
private val ChipColor = Color(0xFFEF7F7F)
private val BgGray    = Color(0xFFF5F5F5)
private val TextGray  = Color(0xFF888888)

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    var isEditMode by remember { mutableStateOf(false) }
    var fullName   by remember { mutableStateOf("Federica") }
    var age        by remember { mutableStateOf("20") }
    var location   by remember { mutableStateOf("Centro, Madrid") }
    var bio        by remember { mutableStateOf("") }
    var budget     by remember { mutableStateOf("600") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    var traits by remember {
        mutableStateOf(
            listOf("Responsable", "Respetuosa", "Limpia",
                "Empatica", "Tranquila", "Organizada",
                "Comunicativa", "Considerada", "Flexible")
        )
    }
    var newTrait by remember { mutableStateOf("") }

    // ── Dialog de logout ─────────────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logout()
                    showLogoutDialog = false
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }) { Text("Cerrar sesión", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Header blanco ────────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Fila título + botón logout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(48.dp))
                    Text(
                        text = if (isEditMode) "Edita tu Perfil" else "Mi Perfil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RoomBlue,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = RoomRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Foto de perfil circular
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDDDDDD))
                        .border(3.dp, RoomBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "https://via.placeholder.com/120",
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$fullName, $age años",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RoomBlue,
                    fontSize = 20.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Sección de rasgos (chips) ────────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = Color.White,
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Rasgos de personalidad",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    traits.forEach { trait ->
                        TraitChip(
                            label = trait,
                            editable = isEditMode,
                            onRemove = { traits = traits - trait }
                        )
                    }
                }

                if (isEditMode) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTrait,
                            onValueChange = { newTrait = it },
                            label = { Text("Añadir rasgo…", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newTrait.isNotBlank() && newTrait !in traits) {
                                    traits = traits + newTrait.trim()
                                    newTrait = ""
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Añadir", tint = RoomBlue)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isEditMode) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = Color.White,
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EditField("Nombre",      fullName) { fullName  = it }
                    EditField("Edad",        age)      { age       = it }
                    EditField("Ubicación",   location) { location  = it }
                    EditField("Bio",         bio)      { bio       = it }
                    EditField("Presupuesto", budget)   { budget    = it }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Botón principal ──────────────────────────────────────────────────
        Button(
            onClick = { isEditMode = !isEditMode },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(containerColor = RoomBlue)
        ) {
            Text(
                text = if (isEditMode) "Guardar cambios" else "Editar Perfil",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Chip de rasgo ─────────────────────────────────────────────────────────────
@Composable
private fun TraitChip(label: String, editable: Boolean, onRemove: () -> Unit) {
    Surface(
        color = ChipColor,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            if (editable) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Eliminar $label",
                    tint = Color.White,
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

@Composable
private fun EditField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}