package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.domain.model.InterestedUserItem
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.InterestedUsersListViewModel
import org.koin.androidx.compose.koinViewModel
import com.example.roommatch_pmdm.ui.theme.RoomBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestedUsersListScreen(
    postId: String,
    navController: NavController,
    viewModel: InterestedUsersListViewModel = koinViewModel()
) {
    val items     by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(postId) { viewModel.loadInterests(postId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Interesados (${items.size})",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                // ── TopAppBar adaptada al tema ────────────────────────────────
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.surface,
                    titleContentColor = RoomBlue,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        when {
            isLoading -> Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = RoomBlue) }

            items.isEmpty() -> Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aún no hay nadie interesado en este anuncio",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.interest.id }) { item ->
                    InterestedUserCard(
                        item          = item,
                        onViewProfile = {
                            navController.navigate(
                                Screen.InterestedUserProfile.createRoute(item.interest.interestedUserId)
                            )
                        },
                        onChat = {
                            navController.navigate(
                                Screen.ChatDetail.createRoute(item.interest.interestedUserId)
                            )
                        }
                    )
                }
            }
        }
    }
}

// ── Card ──────────────────────────────────────────────────────────────────────

@Composable
private fun InterestedUserCard(
    item: InterestedUserItem,
    onViewProfile: () -> Unit,
    onChat: () -> Unit
) {
    val user = item.user

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onViewProfile() },
        elevation = CardDefaults.cardElevation(2.dp),
        // ── fondo de la card adaptado al tema ─────────────────────────────
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Avatar ────────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.size(52.dp),
                shape    = CircleShape,
                color    = RoomBlue.copy(alpha = 0.12f)
            ) {
                if (user?.profileImage?.isNotEmpty() == true) {
                    AsyncImage(
                        model              = user.profileImage,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.clip(CircleShape)
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint     = RoomBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // ── Datos del usuario ─────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = user?.username?.ifEmpty { item.interest.interestedUsername }
                        ?: item.interest.interestedUsername,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                if (user?.location?.isNotEmpty() == true) {
                    Text(
                        text  = "📍 ${user.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
                if ((user?.budget ?: 0) > 0) {
                    Text(
                        text  = "💶 Hasta ${user!!.budget}€/mes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            }

            // ── Botón chat ────────────────────────────────────────────────────
            IconButton(onClick = onChat) {
                Icon(
                    Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Chatear",
                    tint     = RoomBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}