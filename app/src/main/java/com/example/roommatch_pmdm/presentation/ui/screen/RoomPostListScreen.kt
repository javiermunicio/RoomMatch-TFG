package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.RoomPostListViewModel
import org.koin.androidx.compose.koinViewModel
import com.example.roommatch_pmdm.ui.theme.RoomBlue
import com.example.roommatch_pmdm.ui.theme.RoomBlueSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomPostListScreen(
    navController: NavController,
    viewModel: RoomPostListViewModel = koinViewModel()
) {
    val roomPosts        by viewModel.roomPosts.collectAsState()
    val currentUid       by viewModel.currentUserId.collectAsState()
    val hasActiveFilters by viewModel.hasActiveFilters.collectAsState()

    val filterCity       by viewModel.filterCity.collectAsState()
    val filterMaxPrice   by viewModel.filterMaxPrice.collectAsState()
    val filterRoommates  by viewModel.filterRoommates.collectAsState()

    var filtersExpanded  by remember { mutableStateOf(false) }

    val activeFilterCount = remember(filterCity, filterMaxPrice, filterRoommates) {
        listOf(filterCity, filterMaxPrice, filterRoommates).count { it.isNotBlank() }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier        = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color           = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Anuncios de pisos",
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )

                        Box {
                            FilledTonalIconButton(
                                onClick = { filtersExpanded = !filtersExpanded },
                                colors  = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = if (filtersExpanded || hasActiveFilters)
                                        RoomBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor   = if (filtersExpanded || hasActiveFilters)
                                        Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(
                                    Icons.Filled.Tune,
                                    contentDescription = "Filtros",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (activeFilterCount > 0) {
                                Surface(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp),
                                    shape = CircleShape,
                                    color = Color(0xFFE74C3C)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "$activeFilterCount",
                                            fontSize   = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color      = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = filtersExpanded,
                        enter   = expandVertically(tween(250)) + fadeIn(tween(200)),
                        exit    = shrinkVertically(tween(200)) + fadeOut(tween(150))
                    ) {
                        FilterPanel(
                            filterCity        = filterCity,
                            filterMaxPrice    = filterMaxPrice,
                            filterRoommates   = filterRoommates,
                            hasActiveFilters  = hasActiveFilters,
                            onCityChange      = { viewModel.filterCity.value = it },
                            onMaxPriceChange  = { viewModel.filterMaxPrice.value = it },
                            onRoommatesChange = { viewModel.filterRoommates.value = it },
                            onClear           = {
                                viewModel.clearFilters()
                                filtersExpanded = false
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick           = { navController.navigate(Screen.NewRoomPost.route) },
                containerColor    = RoomBlue,
                contentColor      = Color.White,
                icon              = { Icon(Icons.Filled.Add, contentDescription = null) },
                text              = { Text("Publicar", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { innerPadding ->

        if (roomPosts.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape    = CircleShape,
                        color    = RoomBlueSoft
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint     = RoomBlue,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Text(
                        if (hasActiveFilters) "Sin resultados para esos filtros"
                        else "No hay anuncios disponibles",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (hasActiveFilters) "Prueba a cambiar o eliminar algún filtro"
                        else "¡Sé el primero en publicar un piso!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (hasActiveFilters) {
                        OutlinedButton(
                            onClick = { viewModel.clearFilters() },
                            colors  = ButtonDefaults.outlinedButtonColors(contentColor = RoomBlue),
                            border  = androidx.compose.foundation.BorderStroke(1.dp, RoomBlue)
                        ) {
                            Text("Limpiar filtros")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "${roomPosts.size} anuncio${if (roomPosts.size != 1) "s" else ""}${
                            if (hasActiveFilters) " encontrado${if (roomPosts.size != 1) "s" else ""}" else ""
                        }",
                        style  = MaterialTheme.typography.labelMedium,
                        color  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                items(items = roomPosts, key = { it.id }) { post ->
                    RoomPostCard(
                        post     = post,
                        isOwner  = post.ownerId == currentUid,
                        onDelete = { viewModel.delete(post.id) },
                        onClick  = {
                            navController.navigate(Screen.RoomPostDetail.createRoute(post.id))
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}


@Composable
private fun FilterPanel(
    filterCity:        String,
    filterMaxPrice:    String,
    filterRoommates:   String,
    hasActiveFilters:  Boolean,
    onCityChange:      (String) -> Unit,
    onMaxPriceChange:  (String) -> Unit,
    onRoommatesChange: (String) -> Unit,
    onClear:           () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            FilterField(
                value         = filterCity,
                onValueChange = onCityChange,
                label         = "Ciudad",
                placeholder   = "Ej: Madrid",
                leadingIcon   = {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint     = if (filterCity.isNotBlank()) RoomBlue
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterField(
                    value          = filterMaxPrice,
                    onValueChange  = onMaxPriceChange,
                    label          = "Precio máx.",
                    placeholder    = "€/mes",
                    keyboardType   = KeyboardType.Number,
                    modifier       = Modifier.weight(1f),
                    leadingIcon    = {
                        Icon(
                            Icons.Filled.EuroSymbol,
                            contentDescription = null,
                            tint     = if (filterMaxPrice.isNotBlank()) RoomBlue
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                FilterField(
                    value          = filterRoommates,
                    onValueChange  = onRoommatesChange,
                    label          = "Compañeros",
                    placeholder    = "Nº",
                    keyboardType   = KeyboardType.Number,
                    modifier       = Modifier.weight(1f),
                    leadingIcon    = {
                        Icon(
                            Icons.Filled.Group,
                            contentDescription = null,
                            tint     = if (filterRoommates.isNotBlank()) RoomBlue
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            AnimatedVisibility(
                visible = hasActiveFilters,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filterCity.isNotBlank()) {
                        ActiveFilterChip(label = filterCity, onRemove = { onCityChange("") })
                    }
                    if (filterMaxPrice.isNotBlank()) {
                        ActiveFilterChip(label = "≤${filterMaxPrice}€", onRemove = { onMaxPriceChange("") })
                    }
                    if (filterRoommates.isNotBlank()) {
                        ActiveFilterChip(label = "${filterRoommates} comp.", onRemove = { onRoommatesChange("") })
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = onClear,
                        colors  = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE74C3C))
                    ) {
                        Text("Limpiar todo", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterField(
    value:         String,
    onValueChange: (String) -> Unit,
    label:         String,
    placeholder:   String,
    modifier:      Modifier = Modifier.fillMaxWidth(),
    keyboardType:  KeyboardType = KeyboardType.Text,
    leadingIcon:   @Composable (() -> Unit)? = null
) {
    val isActive = value.isNotBlank()
    val borderColor by animateColorAsState(
        targetValue = if (isActive) RoomBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        animationSpec = tween(200),
        label = "borderColor"
    )

    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = modifier,
        singleLine    = true,
        label         = {
            Text(
                label,
                fontSize = 12.sp,
                color    = if (isActive) RoomBlue
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        },
        placeholder   = {
            Text(
                placeholder,
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        },
        leadingIcon   = leadingIcon,
        trailingIcon  = {
            if (isActive) {
                IconButton(
                    onClick  = { onValueChange("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Limpiar",
                        tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape  = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = RoomBlue,
            unfocusedBorderColor    = borderColor,
            focusedLabelColor       = RoomBlue,
            unfocusedLabelColor     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            focusedContainerColor   = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
    )
}

@Composable
private fun ActiveFilterChip(label: String, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = RoomBlue.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, RoomBlue.copy(alpha = 0.35f))
    ) {
        Row(
            modifier          = Modifier.padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                label,
                fontSize   = 12.sp,
                color      = RoomBlue,
                fontWeight = FontWeight.Medium
            )
            IconButton(
                onClick  = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Eliminar filtro",
                    tint     = RoomBlue,
                    modifier = Modifier.size(10.dp)
                )
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

    val cardBg        = MaterialTheme.colorScheme.surface
    val titleColor    = MaterialTheme.colorScheme.onSurface
    val subtitleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
    val pillBg        = MaterialTheme.colorScheme.primaryContainer
    val pillText      = MaterialTheme.colorScheme.onPrimaryContainer
    val chipBg        = MaterialTheme.colorScheme.surfaceVariant
    val chipText      = MaterialTheme.colorScheme.onSurfaceVariant
    val avatarBg      = MaterialTheme.colorScheme.primaryContainer
    val avatarText    = MaterialTheme.colorScheme.onPrimaryContainer
    val dividerColor  = MaterialTheme.colorScheme.outlineVariant

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar anuncio") },
            text  = { Text("¿Eliminar '${post.title}'?") },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showDialog = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFE24B4A))
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Borde azul izquierdo
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(IntrinsicSize.Max)
                    .background(RoomBlue)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(
                        text       = post.title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = titleColor,
                        modifier   = Modifier.weight(1f).padding(end = 8.dp)
                    )
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint     = subtitleColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text  = "${post.address}, ${post.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = pillBg,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text       = "${post.price}€/mes",
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        color      = pillText,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (post.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text     = post.description,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = subtitleColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (post.roommates > 0) {
                        InfoPill(
                            icon     = Icons.Filled.Group,
                            label    = "${post.roommates} compañero${if (post.roommates != 1) "s" else ""}",
                            chipBg   = chipBg,
                            chipText = chipText
                        )
                    }
                    if (post.availableFrom.isNotEmpty()) {
                        InfoPill(
                            icon     = Icons.Filled.CalendarToday,
                            label    = "Desde ${post.availableFrom}",
                            chipBg   = chipBg,
                            chipText = chipText
                        )
                    }
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier.size(22.dp),
                    shape    = CircleShape,
                    color    = avatarBg
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text       = post.ownerName.take(2).uppercase(),
                            fontSize   = 9.sp,
                            color      = avatarText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text  = post.ownerName.ifEmpty { "Propietario" },
                    style = MaterialTheme.typography.labelSmall,
                    color = subtitleColor
                )
            }

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

@Composable
private fun InfoPill(
    icon:     androidx.compose.ui.graphics.vector.ImageVector,
    label:    String,
    chipBg:   Color,
    chipText: Color
) {
    Surface(
        color = chipBg,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment     = Alignment.CenterVertically,
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
                color    = chipText
            )
        }
    }
}