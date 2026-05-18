package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.domain.model.ChatUser
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.ChatListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = koinViewModel()
) {
    val chatUsers by viewModel.chatUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUserId = remember {
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    LaunchedEffect(Unit) { viewModel.refresh() }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Text(
                "Chats",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (chatUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes chats. ¡Haz match con alguien!", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatUsers) { chatUser ->
                    ChatUserItem(
                        chatUser = chatUser,
                        currentUserId = currentUserId
                    ) {
                        navController.navigate(Screen.ChatDetail.createRoute(chatUser.id))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatUserItem(chatUser: ChatUser, currentUserId: String, onItemClick: () -> Unit) {
    val iSentLast = chatUser.lastMessageSenderId == currentUserId
    val hasUnread = !iSentLast && !chatUser.isRead && chatUser.lastMessage != "Toca para chatear"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        onClick   = onItemClick,
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (hasUnread)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier              = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                color    = MaterialTheme.colorScheme.primary,
                shape    = CircleShape
            ) {
                AsyncImage(
                    model        = chatUser.profileImage.ifEmpty { "https://via.placeholder.com/56" },
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    chatUser.username,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = if (hasUnread) FontWeight.ExtraBold else FontWeight.Bold
                )
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (iSentLast && chatUser.lastMessage != "Toca para chatear") {
                        Icon(
                            imageVector = if (chatUser.isRead)
                                Icons.Default.DoneAll
                            else
                                Icons.Default.Check,
                            contentDescription = if (chatUser.isRead) "Leído" else "Enviado",
                            modifier = Modifier.size(13.dp),
                            tint = if (chatUser.isRead)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                    Text(
                        chatUser.lastMessage,
                        style      = MaterialTheme.typography.bodySmall,
                        color      = if (hasUnread)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines   = 1
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (chatUser.timestamp > 0) {
                    val timeText = remember(chatUser.timestamp) {
                        val now  = System.currentTimeMillis()
                        val diff = now - chatUser.timestamp
                        when {
                            diff < 60 * 60 * 1000 ->
                                java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date(chatUser.timestamp))
                            diff < 24 * 60 * 60 * 1000 -> "Ayer"
                            else ->
                                java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
                                    .format(java.util.Date(chatUser.timestamp))
                        }
                    }
                    Text(
                        timeText,
                        fontSize = 11.sp,
                        color    = if (hasUnread)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                if (hasUnread) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        color    = MaterialTheme.colorScheme.primary,
                        shape    = CircleShape
                    ) {}
                }
            }
        }
    }
}