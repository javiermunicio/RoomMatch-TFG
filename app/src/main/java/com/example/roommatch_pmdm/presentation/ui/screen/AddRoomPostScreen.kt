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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
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
import com.example.roommatch_pmdm.presentation.ui.components.ActionMenu
import com.example.roommatch_pmdm.presentation.viewmodel.AddRoomPostViewModel
import org.koin.androidx.compose.koinViewModel

private val RoomBlue = Color(0xFF4A90D9)

@Composable
fun AddRoomPostScreen(
    navController: NavController,
    viewModel: AddRoomPostViewModel = koinViewModel()
) {
    val validationError  by viewModel.validationError.collectAsState()
    val roomPost         by viewModel.roomPost.collectAsState()
    val isSaved          by viewModel.isSaved.collectAsState()
    val isLoading        by viewModel.isLoading.collectAsState()
    val selectedUris     by viewModel.selectedImageUris.collectAsState()
    val uploadProgress   by viewModel.uploadProgress.collectAsState()
    var showDialog       by remember { mutableStateOf(false) }

    // Lanzador para seleccionar múltiples imágenes
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) viewModel.addImages(uris)
    }

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
            text  = {
                Text("¿Publicar '${roomPost.title}' en ${roomPost.city}" +
                        if (selectedUris.isNotEmpty()) " con ${selectedUris.size} imagen(es)?" else "?")
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false; viewModel.save() }) {
                    Text("Publicar")
                }
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

            // ── Selector de imágenes ─────────────────────────────────────────
            ImagePickerSection(
                selectedUris  = selectedUris,
                onAddImages   = { imagePickerLauncher.launch("image/*") },
                onRemoveImage = { viewModel.removeImage(it) }
            )

            HorizontalDivider()

            // ── Campos del formulario ────────────────────────────────────────
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

            // ── Progreso de subida ───────────────────────────────────────────
            uploadProgress?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = RoomBlue)
                    Text(it, style = MaterialTheme.typography.bodySmall, color = RoomBlue)
                }
            }

            // ── Error de validación ──────────────────────────────────────────
            validationError?.let {
                Text(
                    text     = it,
                    color    = Color.Red,
                    style    = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Botones ──────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick   = { viewModel.reset() },
                    modifier  = Modifier.weight(1f)
                ) { Text("Limpiar") }

                Button(
                    onClick  = { showDialog = true },
                    modifier = Modifier.weight(1f),
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
                        Text("Publicar")
                    }
                }
            }
        }
    }
}

// ── Componente selector de imágenes ──────────────────────────────────────────

@Composable
private fun ImagePickerSection(
    selectedUris:  List<Uri>,
    onAddImages:   () -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
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
            Text(
                "${selectedUris.size}/5",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Grid de imágenes + botón añadir
        val items = selectedUris + listOfNotNull(
            if (selectedUris.size < 5) null else null // placeholder logic below
        )

        // Usamos FlowRow para que se adapte automáticamente
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp),
            modifier              = Modifier.fillMaxWidth()
        ) {
            // Thumbnails de imágenes seleccionadas
            selectedUris.forEach { uri ->
                ImageThumbnail(
                    uri      = uri,
                    onRemove = { onRemoveImage(uri) }
                )
            }

            // Botón "+" para añadir más (máximo 5)
            if (selectedUris.size < 5) {
                AddImageButton(onClick = onAddImages)
            }
        }

        if (selectedUris.isEmpty()) {
            Text(
                "Añade hasta 5 fotos para que los interesados puedan ver el piso",
                style    = MaterialTheme.typography.bodySmall,
                color    = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
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
        // Botón eliminar encima de la imagen
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
                fontSize = 11.sp,
                color    = RoomBlue,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddRoomPostScreenPreview() {
    AddRoomPostScreen(rememberNavController())
}