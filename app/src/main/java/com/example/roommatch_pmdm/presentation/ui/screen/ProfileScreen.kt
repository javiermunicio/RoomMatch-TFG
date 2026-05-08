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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.clickable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.material.icons.filled.CameraAlt

private val RoomBlue  = Color(0xFF4A90D9)
private val RoomRed   = Color(0xFFF26B6B)
private val ChipColor = Color(0xFFEF7F7F)
// MaterialTheme.colorScheme.surfaceVariant replaced by MaterialTheme.colorScheme.surfaceVariant
// MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) replaced by MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f)

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    // --- Estado del ViewModel ---
    val isEditing    by viewModel.isEditing.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val isSaved      by viewModel.isSaved.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val username     by viewModel.username.collectAsState()
    val age          by viewModel.age.collectAsState()
    val location     by viewModel.location.collectAsState()
    val bio          by viewModel.bio.collectAsState()
    val budget       by viewModel.budget.collectAsState()
    val habits       by viewModel.selectedHabits.collectAsState()
    val profileImageUrl by viewModel.profileImageUrl.collectAsState()
    val isUploadingImage by viewModel.isUploadingImage.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadProfileImage(it) }
    }

    var newTrait         by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Snackbar al guardar
    LaunchedEffect(isSaved) {
        if (isSaved) viewModel.clearSaved()
    }

    // --- Dialog logout ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text  = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logout()
                    showLogoutDialog = false
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }) { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // --- Header ---
        Surface(
            modifier       = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier             = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment  = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier             = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment    = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(48.dp))
                    Text(
                        text       = if (isEditing) "Edita tu Perfil" else "Mi Perfil",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = RoomBlue,
                        fontSize   = 18.sp
                    )
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar sesión", tint = RoomRed)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        .border(3.dp, RoomBlue, CircleShape)
                        .clickable { if (isEditing) imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = profileImageUrl.ifEmpty { "https://via.placeholder.com/120" },
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Overlay de cámara cuando está en modo edición
                    if (isEditing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUploadingImage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = "Cambiar foto",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val displayAge = if (age.isNotEmpty()) ", $age años" else ""
                Text(
                    text       = "$username$displayAge",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = RoomBlue,
                    fontSize   = 20.sp
                )

                if (location.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                        Text(text = location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(start = 2.dp))
                    }
                }
            }
        }

        // Mensaje de error
        errorMessage?.let { msg ->
            Text(
                text     = msg,
                color = MaterialTheme.colorScheme.error,
                style    = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Chips de hábitos / rasgos ---
        Surface(
            modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            shape           = MaterialTheme.shapes.medium,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Rasgos de personalidad",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier   = Modifier.padding(bottom = 12.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    habits.forEach { trait ->
                        TraitChip(
                            label    = trait,
                            editable = isEditing,
                            onRemove = { viewModel.toggleHabit(trait) }
                        )
                    }
                }

                if (isEditing) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value         = newTrait,
                            onValueChange = { newTrait = it },
                            label         = { Text("Añadir rasgo…", fontSize = 12.sp) },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            if (newTrait.isNotBlank()) {
                                viewModel.toggleHabit(newTrait.trim())
                                newTrait = ""
                            }
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = "Añadir", tint = RoomBlue)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Campos editables ---
        if (isEditing) {
            Surface(
                modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.surface,
                shape           = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EditField("Nombre",      username, viewModel::onUsernameChanged)
                    EditField("Edad",        age,      viewModel::onAgeChanged)
                    EditField("Ubicación",   location, viewModel::onLocationChanged)
                    EditField("Bio",         bio,      viewModel::onBioChanged)
                    EditField("Presupuesto", budget,   viewModel::onBudgetChanged)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // --- Botón principal ---
        Button(
            onClick  = {
                if (isEditing) viewModel.saveProfile() else viewModel.toggleEditMode()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            shape    = MaterialTheme.shapes.extraLarge,
            colors   = ButtonDefaults.buttonColors(containerColor = RoomBlue),
            enabled  = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.surface, strokeWidth = 2.dp)
            } else {
                Text(
                    text       = if (isEditing) "Guardar cambios" else "Editar Perfil",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TraitChip(label: String, editable: Boolean, onRemove: () -> Unit) {
    Surface(color = ChipColor, shape = MaterialTheme.shapes.extraLarge) {
        Row(
            modifier          = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = MaterialTheme.colorScheme.onPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (editable) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onRemove, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Eliminar $label", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
private fun EditField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        label         = { Text(label, fontSize = 12.sp) },
        modifier      = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        singleLine    = true
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}