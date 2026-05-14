package com.example.roommatch_pmdm.presentation.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.AddRoomPostViewModel
import org.koin.androidx.compose.koinViewModel
import com.example.roommatch_pmdm.ui.theme.RoomBlue
import com.example.roommatch_pmdm.ui.theme.RoomBlueSoft
import com.example.roommatch_pmdm.ui.theme.RoomGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoomPostScreen(
    navController: NavController,
    viewModel: AddRoomPostViewModel = koinViewModel()
) {
    val validationError by viewModel.validationError.collectAsState()
    val roomPost        by viewModel.roomPost.collectAsState()
    val isSaved         by viewModel.isSaved.collectAsState()
    val isLoading       by viewModel.isLoading.collectAsState()
    val selectedUris    by viewModel.selectedImageUris.collectAsState()
    val uploadProgress  by viewModel.uploadProgress.collectAsState()
    var showDialog      by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) viewModel.addImages(uris)
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            viewModel.clearSaved()
            navController.navigate(Screen.AddRooms.route) {
                popUpTo(Screen.AddRooms.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    // ── Diálogo de confirmación ────────────────────────────────────────────
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("¿Publicar anuncio?") },
            text  = {
                Text(
                    "Se publicará '${roomPost.title}' en ${roomPost.city}" +
                            if (selectedUris.isNotEmpty()) " con ${selectedUris.size} foto(s)." else "."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false; viewModel.save() },
                    colors  = ButtonDefaults.buttonColors(containerColor = RoomBlue)
                ) { Text("Publicar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                modifier        = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color           = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        "Publicar habitación",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary,
                        modifier   = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Sección fotos ──────────────────────────────────────────────
            FormSection(title = "Fotos del piso") {
                ImagePickerSection(
                    selectedUris  = selectedUris,
                    onAddImages   = { imagePickerLauncher.launch("image/*") },
                    onRemoveImage = { viewModel.removeImage(it) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Sección información básica ─────────────────────────────────
            FormSection(title = "Información básica") {
                FormField(
                    value         = roomPost.title,
                    onValueChange = { viewModel.setTitle(it) },
                    label         = "Título del anuncio",
                    placeholder   = "Ej: Habitación luminosa en Malasaña",
                    icon          = Icons.Filled.Home
                )
                Spacer(modifier = Modifier.height(10.dp))
                FormField(
                    value         = roomPost.description,
                    onValueChange = { viewModel.setDescription(it) },
                    label         = "Descripción",
                    placeholder   = "Describe el piso, normas, ambiente...",
                    icon          = Icons.Filled.Notes,
                    minLines      = 3
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Sección ubicación ──────────────────────────────────────────
            FormSection(title = "Ubicación") {
                FormField(
                    value         = roomPost.address,
                    onValueChange = { viewModel.setAddress(it) },
                    label         = "Dirección",
                    placeholder   = "Calle y número",
                    icon          = Icons.Filled.LocationOn
                )
                Spacer(modifier = Modifier.height(10.dp))
                FormField(
                    value         = roomPost.city,
                    onValueChange = { viewModel.setCity(it) },
                    label         = "Ciudad",
                    placeholder   = "Ej: Madrid",
                    icon          = Icons.Filled.LocationCity
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Sección detalles ───────────────────────────────────────────
            FormSection(title = "Detalles") {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FormField(
                        value         = if (roomPost.price == 0L) "" else roomPost.price.toString(),
                        onValueChange = { viewModel.setPrice(it.toLongOrNull() ?: 0L) },
                        label         = "Precio (€/mes)",
                        placeholder   = "600",
                        icon          = Icons.Filled.EuroSymbol,
                        keyboardType  = KeyboardType.Number,
                        modifier      = Modifier.weight(1f)
                    )
                    FormField(
                        value         = if (roomPost.roommates == 0) "" else roomPost.roommates.toString(),
                        onValueChange = { viewModel.setRoommates(it.toIntOrNull() ?: 0) },
                        label         = "Compañeros",
                        placeholder   = "2",
                        icon          = Icons.Filled.Group,
                        keyboardType  = KeyboardType.Number,
                        modifier      = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                FormField(
                    value         = roomPost.availableFrom,
                    onValueChange = { viewModel.setAvailableFrom(it) },
                    label         = "Disponible desde",
                    placeholder   = "DD/MM/AAAA",
                    icon          = Icons.Filled.CalendarToday
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Progreso de subida ─────────────────────────────────────────
            AnimatedVisibility(
                visible = uploadProgress != null,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                uploadProgress?.let {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color    = RoomBlueSoft,
                        shape    = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier              = Modifier.padding(12.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color       = RoomBlue
                            )
                            Text(it, style = MaterialTheme.typography.bodySmall, color = RoomBlue)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ── Error de validación ────────────────────────────────────────
            AnimatedVisibility(
                visible = validationError != null,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                validationError?.let {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color    = MaterialTheme.colorScheme.errorContainer,
                        shape    = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier          = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
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

            // ── Botones ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick  = { viewModel.reset() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = MaterialTheme.shapes.extraLarge,
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Limpiar", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick  = { showDialog = true },
                    modifier = Modifier.weight(2f).height(52.dp),
                    shape    = MaterialTheme.shapes.extraLarge,
                    colors   = ButtonDefaults.buttonColors(containerColor = RoomBlue),
                    enabled  = !isLoading &&
                            roomPost.title.isNotEmpty() &&
                            roomPost.city.isNotEmpty()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color       = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Publicar anuncio", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Contenedor de sección con título ─────────────────────────────────────────

@Composable
private fun FormSection(
    title:   String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text       = title,
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

// ── Campo de formulario unificado ─────────────────────────────────────────────

@Composable
private fun FormField(
    value:        String,
    onValueChange:(String) -> Unit,
    label:        String,
    placeholder:  String,
    icon:         ImageVector,
    modifier:     Modifier      = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType  = KeyboardType.Text,
    minLines:     Int           = 1
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = modifier,
        label         = { Text(label, fontSize = 12.sp) },
        placeholder   = { Text(placeholder, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)) },
        leadingIcon   = {
            Icon(
                icon,
                contentDescription = null,
                tint     = if (value.isNotBlank()) RoomBlue
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        },
        minLines      = minLines,
        singleLine    = minLines == 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape         = MaterialTheme.shapes.medium,
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = RoomBlue,
            unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            focusedLabelColor       = RoomBlue,
            focusedContainerColor   = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

// ── Selector de imágenes ──────────────────────────────────────────────────────

@Composable
private fun ImagePickerSection(
    selectedUris:  List<Uri>,
    onAddImages:   () -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                "${selectedUris.size}/5 fotos",
                style = MaterialTheme.typography.bodySmall,
                color = if (selectedUris.isNotEmpty()) RoomBlue
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                fontWeight = FontWeight.Medium
            )
            if (selectedUris.isNotEmpty()) {
                // Mini barra de progreso de imágenes
                LinearProgressIndicator(
                    progress       = { selectedUris.size / 5f },
                    modifier       = Modifier.width(80.dp).height(4.dp).clip(CircleShape),
                    color          = RoomBlue,
                    trackColor     = RoomBlueSoft
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp),
            modifier              = Modifier.fillMaxWidth()
        ) {
            selectedUris.forEach { uri ->
                ImageThumbnail(uri = uri, onRemove = { onRemoveImage(uri) })
            }
            if (selectedUris.size < 5) {
                AddImageButton(onClick = onAddImages, isEmpty = selectedUris.isEmpty())
            }
        }

        if (selectedUris.isEmpty()) {
            Text(
                "Añade hasta 5 fotos para mostrar el piso",
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun ImageThumbnail(uri: Uri, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(90.dp)) {
        AsyncImage(
            model              = uri,
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
        )
        IconButton(
            onClick  = onRemove,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .background(Color.White, CircleShape)
                .border(1.dp, Color(0xFFDDDDDD), CircleShape)
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Eliminar imagen",
                tint     = Color(0xFFE24B4A),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun AddImageButton(onClick: () -> Unit, isEmpty: Boolean) {
    Box(
        modifier = Modifier
            .size(if (isEmpty) 120.dp else 90.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.5.dp,
                color = RoomBlue.copy(alpha = if (isEmpty) 0.6f else 1f),
                shape = RoundedCornerShape(10.dp)
            )
            .background(RoomBlueSoft)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Filled.AddPhotoAlternate,
                contentDescription = "Añadir foto",
                tint     = RoomBlue,
                modifier = Modifier.size(if (isEmpty) 32.dp else 24.dp)
            )
            Text(
                if (isEmpty) "Añadir fotos" else "Añadir",
                fontSize   = if (isEmpty) 12.sp else 11.sp,
                color      = RoomBlue,
                fontWeight = FontWeight.Medium
            )
        }
    }
}