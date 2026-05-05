package com.example.roommatch_pmdm.presentation.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
    val newImageUris    by viewModel.newImageUris.collectAsState()
    val uploadProgress  by viewModel.uploadProgress.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) viewModel.addImages(uris)
    }

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
                title = { Text("Editar anuncio", fontWeight = FontWeight.Bold) },
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

                // ── Gestión de imágenes ──────────────────────────────────────
                EditImageSection(
                    existingUrls  = roomPost.images,
                    newUris       = newImageUris,
                    onAddImages   = { imagePickerLauncher.launch("image/*") },
                    onRemoveExisting = { viewModel.removeExistingImage(it) },
                    onRemoveNew      = { viewModel.removeNewImage(it) }
                )

                HorizontalDivider()

                // ── Campos ───────────────────────────────────────────────────
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

                // ── Progreso de subida ───────────────────────────────────────
                uploadProgress?.let {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color       = RoomBlue
                        )
                        Text(it, style = MaterialTheme.typography.bodySmall, color = RoomBlue)
                    }
                }

                // ── Error ────────────────────────────────────────────────────
                validationError?.let {
                    Text(text = it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ── Botón guardar ────────────────────────────────────────────
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

// ── Sección de imágenes ───────────────────────────────────────────────────────

@Composable
private fun EditImageSection(
    existingUrls:     List<String>,
    newUris:          List<Uri>,
    onAddImages:      () -> Unit,
    onRemoveExisting: (String) -> Unit,
    onRemoveNew:      (Uri) -> Unit
) {
    val total = existingUrls.size + newUris.size

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier.fillMaxWidth()
        ) {
            Text(
                "Fotos del piso",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = RoomBlue
            )
            Text("$total/5", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp),
            modifier              = Modifier.fillMaxWidth()
        ) {
            // Imágenes ya subidas (URLs remotas)
            existingUrls.forEach { url ->
                EditImageThumbnail(
                    model    = url,
                    onRemove = { onRemoveExisting(url) }
                )
            }

            // Imágenes nuevas seleccionadas localmente
            newUris.forEach { uri ->
                EditImageThumbnail(
                    model    = uri,
                    onRemove = { onRemoveNew(uri) }
                )
            }

            // Botón "+" si hay hueco
            if (total < 5) {
                AddImageButton(onClick = onAddImages)
            }
        }

        if (total == 0) {
            Text(
                "Añade hasta 5 fotos para que los interesados puedan ver el piso",
                style    = MaterialTheme.typography.bodySmall,
                color    = Color.Gray
            )
        }
    }
}

@Composable
private fun EditImageThumbnail(model: Any, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(90.dp)) {
        AsyncImage(
            model              = model,
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
                tint     = Color.Red,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun AddImageButton(onClick: () -> Unit) {
    Box(
        modifier         = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, RoomBlue, RoundedCornerShape(10.dp))
            .background(RoomBlue.copy(alpha = 0.05f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Añadir imagen",
                tint     = RoomBlue,
                modifier = Modifier.size(28.dp)
            )
            Text(
                "Añadir",
                fontSize   = 11.sp,
                color      = RoomBlue,
                fontWeight = FontWeight.Medium
            )
        }
    }
}