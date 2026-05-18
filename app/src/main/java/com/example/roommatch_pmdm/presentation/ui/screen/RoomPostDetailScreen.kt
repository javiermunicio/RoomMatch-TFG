package com.example.roommatch_pmdm.presentation.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.RoomPostDetailViewModel
import org.koin.androidx.compose.koinViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.example.roommatch_pmdm.ui.theme.RoomBlue
import com.example.roommatch_pmdm.ui.theme.RoomGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomPostDetailScreen(
    postId: String,
    navController: NavController,
    viewModel: RoomPostDetailViewModel = koinViewModel()
) {
    val post           by viewModel.post.collectAsState()
    val isLoading      by viewModel.isLoading.collectAsState()
    val isInterested   by viewModel.isInterested.collectAsState()
    val isOwner        by viewModel.isOwner.collectAsState()
    val interestCount  by viewModel.interestCount.collectAsState()
    val errorMessage   by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(postId) { viewModel.loadPost(postId) }

    LaunchedEffect(successMessage) {
        successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        post?.title ?: "Detalle del anuncio",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (isOwner && post != null) {
                        IconButton(onClick = {
                            navController.navigate(Screen.EditRoomPost.createRoute(postId))
                        }) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Editar anuncio",
                                tint = RoomBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = RoomBlue
                )
            )
        },
        bottomBar = {
            if (post != null) {
                Surface(
                    modifier        = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    if (isOwner) {
                        Button(
                            onClick = {
                                navController.navigate(
                                    Screen.InterestedUsersList.createRoute(postId)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(52.dp),
                            shape  = MaterialTheme.shapes.extraLarge,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (interestCount > 0) RoomGreen else Color(0xFF90CAF9)
                            )
                        ) {
                            Icon(
                                Icons.Filled.People,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (interestCount > 0)
                                    "Ver $interestCount interesado${if (interestCount != 1) "s" else ""}"
                                else
                                    "Sin interesados aún",
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 15.sp
                            )
                        }
                    } else {
                        Button(
                            onClick  = { viewModel.toggleInterest() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(52.dp),
                            shape   = MaterialTheme.shapes.extraLarge,
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = if (isInterested) Color(0xFFE0E0E0) else RoomBlue
                            ),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(22.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector        = if (isInterested) Icons.Filled.CheckCircle else Icons.Filled.Favorite,
                                    contentDescription = null,
                                    modifier           = Modifier.size(20.dp),
                                    tint               = if (isInterested) RoomBlue else MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text       = if (isInterested) "Ya has mostrado interés" else "Me interesa este piso",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 15.sp,
                                    color      = if (isInterested) RoomBlue else MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            isLoading && post == null -> Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = RoomBlue) }

            post == null -> Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Anuncio no encontrado", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }

            else -> PostDetailContent(
                post          = post!!,
                isOwner       = isOwner,
                interestCount = interestCount,
                modifier      = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun PostDetailContent(
    post: RoomPost,
    isOwner: Boolean,
    interestCount: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {

        if (post.images.isNotEmpty()) {
            LazyRow(
                modifier               = Modifier.fillMaxWidth().height(240.dp),
                horizontalArrangement  = Arrangement.spacedBy(2.dp)
            ) {
                items(post.images) { imageUrl ->
                    AsyncImage(
                        model              = imageUrl,
                        contentDescription = null,
                        modifier           = Modifier.fillParentMaxWidth().fillMaxHeight(),
                        contentScale       = ContentScale.Crop
                    )
                }
            }
        } else {
            Box(
                modifier         = Modifier.fillMaxWidth().height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Home, null, Modifier.size(52.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("Sin imágenes", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "${post.address}, ${post.city}",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Surface(color = RoomBlue, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        "${post.price}€/mes",
                        modifier   = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            shape           = MaterialTheme.shapes.medium,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InfoChip(Icons.Filled.Group,         "Compañeros", "${post.roommates}")
                InfoChip(Icons.Filled.CalendarToday, "Disponible", post.availableFrom.ifEmpty { "Consultar" })
                InfoChip(
                    icon       = Icons.Filled.Favorite,
                    label      = "Interesados",
                    value      = "$interestCount",
                    valueColor = if (isOwner && interestCount > 0) RoomGreen else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (post.description.isNotEmpty()) {
            Surface(
                modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.surface,
                shape           = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionTitle("Descripción")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        post.description,
                        style      = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Surface(
            modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            shape           = MaterialTheme.shapes.medium,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape    = MaterialTheme.shapes.extraLarge,
                    color    = RoomBlue.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, null, tint = RoomBlue, modifier = Modifier.size(24.dp))
                    }
                }
                Column {
                    Text("Publicado por", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(
                        post.ownerName.ifEmpty { "Propietario" },
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            shape           = MaterialTheme.shapes.medium,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionTitle("Ubicación")
                Spacer(modifier = Modifier.height(12.dp))

                val fullAddress = "${post.address}, ${post.city}"

                OutlinedButton(
                    onClick = {
                        val encoded = URLEncoder.encode(fullAddress, StandardCharsets.UTF_8.toString())
                        val gmmIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("geo:0,0?q=$encoded")
                        ).apply { setPackage("com.google.android.apps.maps") }

                        if (gmmIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(gmmIntent)
                        } else {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://maps.google.com/?q=$encoded")
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = MaterialTheme.shapes.medium,
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = RoomBlue)
                ) {
                    Icon(Icons.Filled.LocationOn, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(fullAddress, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("Toca para abrir en Google Maps", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(88.dp))
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = RoomBlue, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = valueColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = RoomBlue)
}