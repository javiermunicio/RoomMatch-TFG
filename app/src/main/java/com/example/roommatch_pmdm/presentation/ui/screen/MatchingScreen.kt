package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.domain.model.UserCard
import com.example.roommatch_pmdm.presentation.ui.components.swipeableCard
import com.example.roommatch_pmdm.presentation.viewmodel.MatchingViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.draw.drawBehind

@Composable
fun MatchingScreen(viewModel: MatchingViewModel = koinViewModel()) {
    val userCards = viewModel.userCards.collectAsState()
    val currentIndex = viewModel.currentIndex.collectAsState()
    val showMatchPopup = viewModel.showMatchPopup.collectAsState()
    val matchedUser = viewModel.matchedUser.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "RoomMatch",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E88E5)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading.value) {
            CircularProgressIndicator()
        } else if (currentIndex.value < userCards.value.size) {
            val currentCard = userCards.value[currentIndex.value]

            key(currentIndex.value) {
                UserCardDisplay(
                    userCard = currentCard,
                    onSwipeLeft = { viewModel.onPass() },
                    onSwipeRight = { viewModel.onLike() }
                )
            }

        } else {
            Text("No hay más usuarios disponibles")
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.onPass() },
                modifier = Modifier
                    .size(64.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFFE74C3C)),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White)
            }

            Button(
                onClick = { viewModel.onLike() },
                modifier = Modifier.size(64.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF2ECC71)),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.White)
            }
        }
    }

    if (showMatchPopup.value && matchedUser.value != null) {
        MatchPopup(
            matchedUser.value!!,
            onDismiss = { viewModel.dismissMatchPopup() }
        )
    }
}

@Composable
fun UserCardDisplay(
    userCard: UserCard,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    val cardBlue    = Color(0xFF4A90D9)
    val chipPinkBg  = Color(0xFFFBEAF0)
    val chipPinkFg  = Color(0xFF993556)
    val chipBlueBg  = Color(0xFFE6F1FB)
    val chipBlueFg  = Color(0xFF185FA5)
    val budgetBg    = Color(0xFFEAF3DE)
    val budgetFg    = Color(0xFF3B6D11)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .swipeableCard(
                onSwipeLeft  = onSwipeLeft,
                onSwipeRight = onSwipeRight
            ),
        shape     = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(6.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {

            // ── Foto + nombre superpuesto ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                AsyncImage(
                    model        = userCard.profileImage.ifEmpty { "https://via.placeholder.com/400x260" },
                    contentDescription = "${userCard.username}, ${userCard.age}",
                    modifier     = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradiente oscuro en la parte inferior de la imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC000000))
                            )
                        )
                )
                // Nombre + ubicación encima del gradiente
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 14.dp, end = 16.dp)
                ) {
                    val displayAge = if (userCard.age > 0) ", ${userCard.age}" else ""
                    Text(
                        text       = "${userCard.username}$displayAge",
                        color      = Color.White,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (userCard.location.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier          = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint     = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text     = " ${userCard.location}",
                                color    = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ── Cuerpo de la tarjeta ──────────────────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Bio
                if (userCard.bio.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind { /* borde izquierdo azul */ }
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(IntrinsicSize.Min)
                                .background(cardBlue, shape = RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text       = userCard.bio,
                            fontSize   = 13.sp,
                            color      = Color(0xFF555555),
                            lineHeight = 19.sp,
                            maxLines   = 3,
                            overflow   = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                }

                // Hábitos / Personalidad
                if (userCard.habits.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Personalidad",
                            fontSize    = 11.sp,
                            fontWeight  = FontWeight.Medium,
                            color       = Color(0xFF999999),
                            letterSpacing = 0.06.sp
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement   = Arrangement.spacedBy(6.dp)
                        ) {
                            userCard.habits.forEach { habit ->
                                Surface(
                                    color  = chipPinkBg,
                                    shape  = CircleShape,
                                    border = BorderStroke(0.5.dp, Color(0xFFED93B1))
                                ) {
                                    Text(
                                        text     = habit,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color    = chipPinkFg,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Preferencias
                if (userCard.preferences.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Preferencias",
                            fontSize    = 11.sp,
                            fontWeight  = FontWeight.Medium,
                            color       = Color(0xFF999999),
                            letterSpacing = 0.06.sp
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement   = Arrangement.spacedBy(6.dp)
                        ) {
                            userCard.preferences.forEach { pref ->
                                Surface(
                                    color  = chipBlueBg,
                                    shape  = CircleShape,
                                    border = BorderStroke(0.5.dp, Color(0xFF85B7EB))
                                ) {
                                    Text(
                                        text     = pref,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color    = chipBlueFg,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Presupuesto
                if (userCard.age > 0 || true) { // siempre mostramos la fila de info extra
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    // Fila: presupuesto (si existe en el modelo)
                    // UserCard no tiene budget, solo mostramos lo disponible:
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        // Puedes añadir budget a UserCard; de momento mostramos un chip neutral
                        Surface(
                            color  = budgetBg,
                            shape  = CircleShape,
                            border = BorderStroke(0.5.dp, Color(0xFF97C459))
                        ) {
                            Row(
                                modifier          = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.EuroSymbol,
                                    contentDescription = null,
                                    tint     = budgetFg,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Busca piso compartido",
                                    color    = budgetFg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun MatchPopup(userCard: UserCard, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "¡Match!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE74C3C)
            )
        },
        text = {
            Text("¡Acabas de hacer match con ${userCard.username}!")
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(Color(0xFF2ECC71))) {
                Text("¡Genial!")
            }
        }
    )
}
@Preview
@Composable
fun MatchingScreenPreview() {
    MatchingScreen()
}