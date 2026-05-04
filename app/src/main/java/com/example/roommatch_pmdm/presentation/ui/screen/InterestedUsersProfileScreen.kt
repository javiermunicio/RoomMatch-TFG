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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.model.User
import com.example.roommatch_pmdm.presentation.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val RoomBlue  = Color(0xFF4A90D9)
private val ChipColor = Color(0xFFEF7F7F)
private val TextGray  = Color(0xFF888888)
private val BgGray    = Color(0xFFF5F5F5)

// ── ViewModel inline (simple, solo carga un usuario) ────────────────────────

class InterestedUserProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user      = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.getUser(userId).fold(
                onSuccess = { _user.value = it },
                onFailure = { /* usuario no encontrado */ }
            )
            _isLoading.value = false
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Color.White,
                    titleContentColor = RoomBlue
                )
            )
        },
        bottomBar = {
            // Botón de chat siempre visible en la parte inferior
            if (user != null) {
                Surface(
                    modifier        = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color           = Color.White
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
            ) { Text("Perfil no disponible", color = TextGray) }

            else -> UserProfileContent(
                user     = user!!,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun UserProfileContent(user: User, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgGray)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Surface(
            modifier        = Modifier.fillMaxWidth(),
            color           = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier         = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDDDDDD))
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
                            tint               = Color(0xFFAAAAAA),
                            modifier           = Modifier.size(52.dp)
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
                            tint               = TextGray,
                            modifier           = Modifier.size(14.dp)
                        )
                        Text(
                            user.location,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = TextGray,
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
                color           = Color.White,
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
                        color      = Color(0xFF424242),
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
                color           = Color.White,
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
                            color = TextGray
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
                color           = Color.White,
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