package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.unit.sp

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
            text  = { Text("¿Eliminar '${post.title}'?") },
            confirmButton = {
                Button(onClick = { onDelete(); showDialog = false }) { Text("Eliminar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        // ── Franja azul lateral + cabecera ────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth()) {
            // Borde azul izquierdo
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(IntrinsicSize.Max)  // ← se adapta a la altura del contenido
                    .background(RoomBlue)
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 12.dp)) {

                // Título + botón borrar
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(
                        text       = post.title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color(0xFF1A1A1A),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)                    )
                    if (isOwner) {
                        IconButton(
                            onClick  = { showDialog = true },
                            modifier = Modifier.size(28.dp).offset(x = 4.dp, y = (-2).dp)
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Eliminar",
                                tint     = Color(0xFFE24B4A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Dirección
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint     = Color(0xFF888888),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text  = "${post.address}, ${post.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF888888)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Precio en pill
                Surface(
                    color = Color(0xFFEBF4FF),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text       = "${post.price}€/mes",
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        color      = Color(0xFF0C447C),
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Descripción (máx 2 líneas)
                if (post.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text     = post.description,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = Color(0xFF666666),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chips de compañeros y fecha
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (post.roommates > 0) {
                        InfoPill(
                            icon  = Icons.Filled.Group,
                            label = "${post.roommates} compañero${if (post.roommates != 1) "s" else ""}"
                        )
                    }
                    if (post.availableFrom.isNotEmpty()) {
                        InfoPill(
                            icon  = Icons.Filled.CalendarToday,
                            label = "Desde ${post.availableFrom}"
                        )
                    }
                }
            }
        }

        // ── Footer ────────────────────────────────────────────────────────
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Avatar + propietario
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier.size(22.dp),
                    shape    = CircleShape,
                    color    = Color(0xFFEBF4FF)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text     = post.ownerName.take(2).uppercase(),
                            fontSize = 9.sp,
                            color    = Color(0xFF0C447C),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text     = post.ownerName.ifEmpty { "Propietario" },
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Color(0xFF888888)
                )
            }

            // "Ver detalle →"
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text       = "Ver detalle",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = RoomBlue,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint     = RoomBlue,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

// ── Chip pequeño de información ───────────────────────────────────────────────
@Composable
private fun InfoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        color = Color(0xFFF5F5F5),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint     = RoomBlue,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text     = label,
                fontSize = 11.sp,
                color    = Color(0xFF555555)
            )
        }
    }
}