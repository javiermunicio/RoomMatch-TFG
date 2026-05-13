package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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
import com.example.roommatch_pmdm.domain.model.User
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.InterestedUserProfileViewModel
import org.koin.androidx.compose.koinViewModel
import com.example.roommatch_pmdm.ui.theme.RoomBlue
import com.example.roommatch_pmdm.ui.theme.ChipColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestedUsersProfileScreen(
    userId: String,
    navController: NavController,
    viewModel: InterestedUserProfileViewModel = koinViewModel()
) {
    val user      by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(userId) { viewModel.loadUser(userId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        user?.username ?: "Perfil",
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
                    containerColor         = MaterialTheme.colorScheme.surface,
                    titleContentColor      = RoomBlue,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (user != null) {
                Surface(
                    modifier        = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    // ── bottom bar adaptada al tema ───────────────────────────
                    color           = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick  = {
                            navController.navigate(Screen.ChatDetail.createRoute(userId))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp),
                        shape  = MaterialTheme.shapes.extraLarge,
                        colors = ButtonDefaults.buttonColors(containerColor = RoomBlue)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Enviar mensaje",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 15.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            isLoading -> Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = RoomBlue) }

            user == null -> Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Perfil no disponible",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            else -> UserProfileContent(
                user     = user!!,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

// ── Contenido del perfil ──────────────────────────────────────────────────────

@Composable
private fun UserProfileContent(user: User, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // ── fondo adaptado al tema ────────────────────────────────────────
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Surface(
            modifier        = Modifier.fillMaxWidth(),
            color           = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        // fondo del avatar adaptado al tema
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(3.dp, RoomBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.profileImage.isNotEmpty()) {
                        AsyncImage(
                            model              = user.profileImage,
                            contentDescription = null,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val displayAge = if (user.age > 0) ", ${user.age} años" else ""
                Text(
                    "${user.username}$displayAge",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = RoomBlue
                )

                if (user.location.isNotEmpty()) {
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
                            user.location,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Bio ──────────────────────────────────────────────────────────────
        if (user.bio.isNotEmpty()) {
            Surface(
                modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color           = MaterialTheme.colorScheme.surface,
                shape           = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Sobre mí",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = RoomBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        user.bio,
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Presupuesto ──────────────────────────────────────────────────────
        if (user.budget > 0) {
            Surface(
                modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color           = MaterialTheme.colorScheme.surface,
                shape           = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "💶",
                        fontSize = 22.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            "Presupuesto máximo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            "${user.budget}€/mes",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = RoomBlue
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Hábitos ──────────────────────────────────────────────────────────
        if (user.habits.isNotEmpty()) {
            Surface(
                modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color           = MaterialTheme.colorScheme.surface,
                shape           = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Rasgos de personalidad",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = RoomBlue
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(8.dp)
                    ) {
                        user.habits.forEach { habit ->
                            Surface(
                                color = ChipColor,
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Text(
                                    habit,
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
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Espacio para el botón inferior
        Spacer(modifier = Modifier.height(88.dp))
    }
}