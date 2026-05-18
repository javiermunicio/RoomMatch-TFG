package com.example.roommatch_pmdm.presentation.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.ProfileViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.ThemeViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import com.example.roommatch_pmdm.ui.theme.RoomBlue
import com.example.roommatch_pmdm.ui.theme.RoomBlueSoft
import com.example.roommatch_pmdm.ui.theme.RoomRed
import com.example.roommatch_pmdm.ui.theme.ChipColor

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val context  = LocalContext.current
    val activity = context as ComponentActivity
    val themeViewModel: ThemeViewModel = activity.getViewModel()
    val isDark by themeViewModel.isDarkTheme.collectAsState()

    val isEditing        by viewModel.isEditing.collectAsState()
    val isLoading        by viewModel.isLoading.collectAsState()
    val isSaved          by viewModel.isSaved.collectAsState()
    val errorMessage     by viewModel.errorMessage.collectAsState()
    val username         by viewModel.username.collectAsState()
    val age              by viewModel.age.collectAsState()
    val location         by viewModel.location.collectAsState()
    val bio              by viewModel.bio.collectAsState()
    val budget           by viewModel.budget.collectAsState()
    val habits           by viewModel.selectedHabits.collectAsState()
    val profileImageUrl  by viewModel.profileImageUrl.collectAsState()
    val isUploadingImage by viewModel.isUploadingImage.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.uploadProfileImage(it) } }

    var newTrait         by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isSaved) {
        if (isSaved) viewModel.clearSaved()
    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text  = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoomRed)
                ) { Text("Cerrar sesión", color = Color.White) }
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

        Surface(
            modifier        = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp,
            color           = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    if (isEditing) "Editar perfil" else "Mi perfil",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { themeViewModel.toggleTheme() }) {
                        Icon(
                            imageVector        = if (isDark) Icons.Filled.LightMode
                            else Icons.Filled.DarkMode,
                            contentDescription = "Cambiar tema",
                            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint               = RoomRed
                        )
                    }
                }
            }
        }
        Surface(
            modifier        = Modifier.fillMaxWidth(),
            color           = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier         = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(3.dp, if (isEditing) RoomBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                        .clickable(enabled = isEditing) { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model              = profileImageUrl.ifEmpty { "https://via.placeholder.com/110" },
                        contentDescription = "Foto de perfil",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                    Column() {
                        AnimatedVisibility(
                            visible = isEditing,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.38f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploadingImage) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Filled.CameraAlt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val displayAge = if (age.isNotEmpty()) ", $age años" else ""
                Text(
                    "$username$displayAge",
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
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            location,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }

                if (bio.isNotEmpty() && !isEditing) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        bio,
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }

                if (budget.isNotEmpty() && budget != "0" && !isEditing) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = RoomBlueSoft,
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Row(
                            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💶", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Hasta ${budget}€/mes",
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = RoomBlue
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(
            visible = isEditing,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier            = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileFormSection(title = "Datos personales") {
                    ProfileFormField(
                        value         = username,
                        onValueChange = { viewModel.onUsernameChanged(it) },
                        label         = "Nombre de usuario",
                        icon          = Icons.Filled.Person
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileFormField(
                            value         = age,
                            onValueChange = { viewModel.onAgeChanged(it) },
                            label         = "Edad",
                            icon          = Icons.Filled.Cake,
                            keyboardType  = KeyboardType.Number,
                            modifier      = Modifier.weight(1f)
                        )
                        ProfileFormField(
                            value         = budget,
                            onValueChange = { viewModel.onBudgetChanged(it) },
                            label         = "Presupuesto (€)",
                            icon          = Icons.Filled.EuroSymbol,
                            keyboardType  = KeyboardType.Number,
                            modifier      = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    ProfileFormField(
                        value         = location,
                        onValueChange = { viewModel.onLocationChanged(it) },
                        label         = "Ciudad",
                        icon          = Icons.Filled.LocationOn
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ProfileFormField(
                        value         = bio,
                        onValueChange = { viewModel.onBioChanged(it) },
                        label         = "Sobre mí",
                        icon          = Icons.Filled.Notes,
                        minLines      = 3
                    )
                }

                ProfileFormSection(title = "Rasgos de personalidad") {
                    if (habits.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement   = Arrangement.spacedBy(8.dp),
                            modifier              = Modifier.fillMaxWidth()
                        ) {
                            habits.forEach { trait ->
                                EditableTraitChip(
                                    label    = trait,
                                    onRemove = { viewModel.toggleHabit(trait) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value         = newTrait,
                            onValueChange = { newTrait = it },
                            label         = { Text("Añadir rasgo...", fontSize = 12.sp) },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            shape         = MaterialTheme.shapes.medium,
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = RoomBlue,
                                focusedLabelColor    = RoomBlue,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        )
                        FilledTonalIconButton(
                            onClick = {
                                if (newTrait.isNotBlank()) {
                                    viewModel.toggleHabit(newTrait.trim())
                                    newTrait = ""
                                }
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = RoomBlueSoft,
                                contentColor   = RoomBlue
                            )
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Añadir rasgo")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        AnimatedVisibility(
            visible = !isEditing && habits.isNotEmpty(),
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color           = MaterialTheme.colorScheme.surface,
                shape           = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Rasgos de personalidad",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier   = Modifier.padding(bottom = 12.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(8.dp)
                    ) {
                        habits.forEach { trait ->
                            Surface(
                                color = ChipColor,
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Text(
                                    trait,
                                    modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color      = Color.White,
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(
            visible = errorMessage != null,
            enter   = fadeIn(),
            exit    = fadeOut()
        ) {
            errorMessage?.let {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color    = MaterialTheme.colorScheme.errorContainer,
                    shape    = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier              = Modifier.padding(12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Column(
            modifier            = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick  = {
                    if (isEditing) viewModel.saveProfile() else viewModel.toggleEditMode()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = MaterialTheme.shapes.extraLarge,
                colors   = ButtonDefaults.buttonColors(containerColor = RoomBlue),
                enabled  = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector        = if (isEditing) Icons.Filled.Save else Icons.Filled.Edit,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isEditing) "Guardar cambios" else "Editar perfil",
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 15.sp
                    )
                }
            }

            AnimatedVisibility(
                visible = isEditing,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                OutlinedButton(
                    onClick  = { viewModel.toggleEditMode() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = MaterialTheme.shapes.extraLarge,
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancelar", fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileFormSection(
    title:   String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = RoomBlue,
            modifier   = Modifier.padding(bottom = 8.dp, start = 2.dp)
        )
        Surface(
            modifier        = Modifier.fillMaxWidth(),
            color           = MaterialTheme.colorScheme.surface,
            shape           = MaterialTheme.shapes.medium,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun ProfileFormField(
    value:         String,
    onValueChange: (String) -> Unit,
    label:         String,
    icon:          ImageVector,
    modifier:      Modifier     = Modifier.fillMaxWidth(),
    keyboardType:  KeyboardType = KeyboardType.Text,
    minLines:      Int          = 1
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = modifier,
        label         = { Text(label, fontSize = 12.sp) },
        leadingIcon   = {
            Icon(
                icon,
                contentDescription = null,
                tint     = if (value.isNotBlank()) RoomBlue
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        },
        minLines        = minLines,
        singleLine      = minLines == 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape           = MaterialTheme.shapes.medium,
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = RoomBlue,
            unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            focusedLabelColor       = RoomBlue,
            focusedContainerColor   = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun EditableTraitChip(label: String, onRemove: () -> Unit) {
    Surface(
        color = ChipColor,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier          = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                label,
                color      = Color.White,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium
            )
            IconButton(
                onClick  = onRemove,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Eliminar $label",
                    tint     = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}