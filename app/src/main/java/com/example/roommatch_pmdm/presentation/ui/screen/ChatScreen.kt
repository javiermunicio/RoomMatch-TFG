package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.domain.model.ChatUser
import com.example.roommatch_pmdm.presentation.viewmodel.ChatListViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.ChatDetailViewModel

@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = viewModel()
) {
    val chatUsers = viewModel.chatUsers.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shadowElevation = 4.dp
        ) {
            Text(
                "RoomMatch",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E88E5),
                modifier = Modifier.padding(16.dp)
            )
        }

        Text(
            "Chats",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E88E5),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (isLoading.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatUsers.value) { chatUser ->
                    ChatUserItem(chatUser) {
                        // navController.navigate(Screen.ChatDetail.createRoute(chatUser.id))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatUserItem(chatUser: ChatUser, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                color = Color(0xFF1E88E5),
                shape = CircleShape
            ) {
                AsyncImage(
                    model = chatUser.profileImage.ifEmpty { "https://via.placeholder.com/56" },
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    chatUser.username,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    chatUser.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "2h",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                if (!chatUser.isRead) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        color = Color.Red,
                        shape = CircleShape
                    ) {}
                } else {
                    Text("✔", color = Color(0xFF1E88E5))
                }
            }
        }
    }
}

@Composable
fun ChatDetailScreen(
    chatUserId: String,
    viewModel: ChatDetailViewModel = viewModel()
) {
    val messages = viewModel.messages.collectAsState()
    val messageInput = viewModel.messageInput.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.value) { message ->
                MessageBubble(message)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageInput.value,
                onValueChange = { viewModel.onMessageInputChanged(it) },
                placeholder = { Text("Escribe un mensaje...") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            )

            IconButton(onClick = { viewModel.sendMessage(chatUserId) }) {
                Icon(Icons.Filled.Send, contentDescription = null)
            }
        }
    }
}

@Composable
fun MessageBubble(message: com.example.roommatch_pmdm.domain.model.ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.senderId == "currentUser") Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 250.dp),
            color = if (message.senderId == "currentUser") Color(0xFF1E88E5) else Color(0xFFEEEEEE),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                message.content,
                modifier = Modifier.padding(12.dp),
                color = if (message.senderId == "currentUser") Color.White else Color.Black
            )
        }
    }
}
