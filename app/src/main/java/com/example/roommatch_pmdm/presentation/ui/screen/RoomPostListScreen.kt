package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.RoomPostListViewModel
import org.koin.androidx.compose.koinViewModel
private val RoomBlue = Color(0xFF4A90D9)
@Composable
fun RoomPostListScreen(
    navController: NavController,
    viewModel: RoomPostListViewModel = koinViewModel()
) {
    val roomPosts       by viewModel.roomPosts.collectAsState()
    val currentUid      by viewModel.currentUserId.collectAsState()
    val hasActiveFilters by viewModel.hasActiveFilters.collectAsState()

    val filterCity      by viewModel.filterCity.collectAsState()
    val filterMaxPrice  by viewModel.filterMaxPrice.collectAsState()
    val filterRoommates by viewModel.filterRoommates.collectAsState()

    var filtersExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { navController.navigate(Screen.NewRoomPost.route) },
                containerColor = RoomBlue
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Publicar habitación", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // ── Barra de filtros ─────────────────────────────────────────────
            FilterBar(
                expanded         = filtersExpanded,
                hasActiveFilters = hasActiveFilters,
                filterCity       = filterCity,
                filterMaxPrice   = filterMaxPrice,
                filterRoommates  = filterRoommates,
                onToggle         = { filtersExpanded = !filtersExpanded },
                onCityChange     = { viewModel.filterCity.value = it },
                onMaxPriceChange = { viewModel.filterMaxPrice.value = it },
                onRoommatesChange = { viewModel.filterRoommates.value = it },
                onClear          = { viewModel.clearFilters(); filtersExpanded = false }
            )

            // ── Lista de anuncios ────────────────────────────────────────────
            if (roomPosts.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (hasActiveFilters) "No hay anuncios con esos filtros"
                            else "No hay anuncios disponibles",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (hasActiveFilters) {
                            TextButton(onClick = { viewModel.clearFilters() }) {
                                Text("Limpiar filtros", color = RoomBlue)
                            }
                        } else {
                            Text(
                                "¡Sé el primero en publicar!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier        = Modifier.fillMaxSize(),
                    contentPadding  = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = roomPosts, key = { it.id }) { post ->
                        RoomPostCard(
                            post    = post,
                            isOwner = post.ownerId == currentUid,
                            onDelete = { viewModel.delete(post.id) },
                            onClick  = {
                                navController.navigate(Screen.RoomPostDetail.createRoute(post.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Panel de filtros ──────────────────────────────────────────────────────────

@Composable
private fun FilterBar(
    expanded:          Boolean,
    hasActiveFilters:  Boolean,
    filterCity:        String,
    filterMaxPrice:    String,
    filterRoommates:   String,
    onToggle:          () -> Unit,
    onCityChange:      (String) -> Unit,
    onMaxPriceChange:  (String) -> Unit,
    onRoommatesChange: (String) -> Unit,
    onClear:           () -> Unit
) {
    Surface(
        modifier        = Modifier.fillMaxWidth(),
        color           = Color.White,
        shadowElevation = 2.dp
    ) {
        Column {
            // Cabecera del panel
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.FilterList,
                        contentDescription = "Filtros",
                        tint     = RoomBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Filtros",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = RoomBlue
                    )
                    if (hasActiveFilters) {
                        Surface(
                            color = RoomBlue,
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                "Activos",
                                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color      = Color.White,
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (hasActiveFilters) {
                    IconButton(
                        onClick  = onClear,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Limpiar filtros",
                            tint     = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Campos de filtro (animados)
            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Ciudad
                    OutlinedTextField(
                        value         = filterCity,
                        onValueChange = onCityChange,
                        label         = { Text("Ciudad") },
                        placeholder   = { Text("Ej: Madrid") },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        trailingIcon  = {
                            if (filterCity.isNotBlank()) {
                                IconButton(onClick = { onCityChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = null,
                                        modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    )

                    // Precio máximo y compañeros en la misma fila
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value         = filterMaxPrice,
                            onValueChange = onMaxPriceChange,
                            label         = { Text("Precio máx. (€)") },
                            placeholder   = { Text("Ej: 600") },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon  = {
                                if (filterMaxPrice.isNotBlank()) {
                                    IconButton(onClick = { onMaxPriceChange("") }) {
                                        Icon(Icons.Filled.Close, contentDescription = null,
                                            modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        )

                        OutlinedTextField(
                            value         = filterRoommates,
                            onValueChange = onRoommatesChange,
                            label         = { Text("Compañeros") },
                            placeholder   = { Text("Ej: 2") },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon  = {
                                if (filterRoommates.isNotBlank()) {
                                    IconButton(onClick = { onRoommatesChange("") }) {
                                        Icon(Icons.Filled.Close, contentDescription = null,
                                            modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
@Composable
fun RoomPostCard(
    post: RoomPost,
    isOwner: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar anuncio") },
            text = { Text("¿Eliminar '${post.title}'?") },
            confirmButton = {
                Button(onClick = { onDelete(); showDialog = false }) { Text("Eliminar") }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },  // ← tap en toda la card
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (isOwner) {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Color.Red)
                    }
                }
            }

            Text("📍 ${post.address}, ${post.city}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("💶 ${post.price}€/mes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

            if (post.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(post.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (post.availableFrom.isNotEmpty()) {
                        Text("Disponible desde: ${post.availableFrom}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Text("Publicado por: ${post.ownerName}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                // Indicador visual de "ver más"
                Text(
                    "Ver detalle →",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4A90D9),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}