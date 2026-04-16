package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.roommatch_pmdm.presentation.viewmodel.MatchingViewModel

@Composable
fun MatchingScreen(viewModel: MatchingViewModel = viewModel()) {
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
            UserCardDisplay(currentCard)
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
fun UserCardDisplay(userCard: UserCard) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
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
                    color = Color.Gray
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
