package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.domain.model.UserCard
import com.example.roommatch_pmdm.presentation.ui.components.swipeableCard
import com.example.roommatch_pmdm.presentation.viewmodel.MatchingViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MatchingScreen(navController: androidx.navigation.NavController? = null, viewModel: MatchingViewModel = koinViewModel()) {
    val userCards = viewModel.userCards.collectAsState()
    val currentIndex = viewModel.currentIndex.collectAsState()
    val showMatchPopup = viewModel.showMatchPopup.collectAsState()
    val matchedUser = viewModel.matchedUser.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "RoomMatch",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
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
            userCard      = matchedUser.value!!,
            onDismiss     = { viewModel.dismissMatchPopup() },
            navController = navController
        )
    }
}

@Composable
fun UserCardDisplay(
    userCard: UserCard,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            // Añadimos el modificador aquí
            .swipeableCard(
                onSwipeLeft = onSwipeLeft,
                onSwipeRight = onSwipeRight
            ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        // ... (El resto de tu código para mostrar la imagen, nombre, etc. se queda igual)
        Column {
            AsyncImage(
                model = userCard.profileImage.ifEmpty { "https://via.placeholder.com/400x300" },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "${userCard.username}, ${userCard.age}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "📍 ${userCard.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    userCard.habits.forEach { habit ->
                        AssistChip(
                            onClick = {},
                            label = { Text(habit, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MatchPopup(userCard: UserCard, onDismiss: () -> Unit, navController: androidx.navigation.NavController? = null) {
    val primaryBlue   = Color(0xFF1E88E5)
    val warmBeige     = Color(0xFFFFF8F0)
    val roofBrown     = Color(0xFF8D6E63)
    val wallCream     = Color(0xFFFFF3E0)
    val accentGold    = Color(0xFFFFA000)

    // Animación de escala al aparecer
    val scale = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessMedium
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                // ── Tejado ────────────────────────────────────────────────
                HousRoofShape(roofBrown, accentGold)

                // ── Cuerpo de la tarjeta ──────────────────────────────────
                Column(
                    modifier = Modifier
                        .padding(top = 48.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(wallCream)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(52.dp)) // espacio para el avatar

                    // Título
                    Text(
                        "🏠 ¡Nueva conexión!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = roofBrown,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        "Compartid algo más que paredes",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 1.dp)
                    Spacer(Modifier.height(14.dp))

                    // Nombre y ubicación
                    Text(
                        userCard.username,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (userCard.location.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text("📍", fontSize = 12.sp)
                            Spacer(Modifier.width(3.dp))
                            Text(
                                userCard.location,
                                fontSize = 13.sp,
                                color = Color(0xFF8D6E63)
                            )
                        }
                    }

                    // Hábitos
                    if (userCard.habits.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            userCard.habits.take(4).forEach { habit ->
                                Surface(
                                    shape  = RoundedCornerShape(50),
                                    color  = primaryBlue.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, primaryBlue.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        habit,
                                        fontSize = 11.sp,
                                        color    = primaryBlue,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    // Botones
                    Button(
                        onClick = {
                            onDismiss()
                            navController?.navigate(
                                com.example.roommatch_pmdm.presentation.navigation.Screen.ChatDetail
                                    .createRoute(userCard.id)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape  = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                    ) {
                        Text("💬 Enviar mensaje", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape  = RoundedCornerShape(50),
                        border = BorderStroke(1.5.dp, roofBrown.copy(alpha = 0.5f))
                    ) {
                        Text(
                            "Seguir explorando",
                            color      = roofBrown,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp
                        )
                    }
                }

                // ── Avatar flotante centrado sobre el cuerpo ──────────────
                Surface(
                    modifier = Modifier
                        .size(90.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = 10.dp)
                        .border(4.dp, wallCream, CircleShape),
                    shape = CircleShape,
                    shadowElevation = 8.dp
                ) {
                    AsyncImage(
                        model              = userCard.profileImage.ifEmpty { "https://via.placeholder.com/90" },
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }

                // ── Estrella decorativa dorada ────────────────────────────
                Surface(
                    modifier  = Modifier
                        .size(28.dp)
                        .align(Alignment.TopCenter)
                        .offset(x = 38.dp, y = 8.dp),
                    shape = CircleShape,
                    color = accentGold
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✨", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ── Tejado SVG dibujado con Canvas ────────────────────────────────────────────
@Composable
fun HousRoofShape(roofColor: Color, accentColor: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 0.dp)
    ) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, h)
            lineTo(w * 0.5f, 0f)   // pico central
            lineTo(w, h)
            close()
        }
        drawPath(path, roofColor)

        // Línea de borde del tejado
        drawPath(
            path,
            color     = accentColor,
            style     = Stroke(width = 3f)
        )

        // Chimenea (pequeño rectángulo a la derecha del pico)
        drawRect(
            color   = roofColor.copy(red = roofColor.red * 0.85f),
            topLeft = Offset(w * 0.65f, h * 0.25f),
            size    = Size(w * 0.06f, h * 0.45f)
        )
    }
}
@Preview
@Composable
fun MatchingScreenPreview() {
    MatchingScreen()
}