package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.domain.model.ChatMessage
import com.example.roommatch_pmdm.domain.model.ChatUser
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.ChatDetailViewModel
import com.example.roommatch_pmdm.presentation.viewmodel.ChatListViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

// ── Colores de burbuja ────────────────────────────────────────────────────────
private val BubbleMe    = Color(0xFF1E88E5)
private val BubbleOther = Color(0xFFEEEEEE)
private val TextMe      = Color.White
private val TextOther   = Color(0xFF212121)

// ── ChatListScreen ────────────────────────────────────────────────────────────
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = koinViewModel()
) {
    val chatUsers by viewModel.chatUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Text(
                "Chats",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E88E5),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (chatUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes chats. ¡Haz match con alguien!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatUsers) { chatUser ->
                    ChatUserItem(chatUser) {
                        navController.navigate(Screen.ChatDetail.createRoute(chatUser.id))
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
        onClick = onItemClick,
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

            if (!chatUser.isRead) {
                Surface(
                    modifier = Modifier.size(10.dp),
                    color = Color.Red,
                    shape = CircleShape
                ) {}
            } else {
                Text("✔", color = Color(0xFF1E88E5), fontSize = 12.sp)
            }
        }
    }
}

// ── ChatDetailScreen ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatUserId: String,
    navController: NavController? = null,
    viewModel: ChatDetailViewModel = koinViewModel()
) {
    val messages     by viewModel.messages.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val currentUid   by viewModel.currentUserIdFlow.collectAsState()
    val listState    = rememberLazyListState()

    // AQUÍ ESTÁ EL CAMBIO 1: Avisamos de que entramos al chat para leer
    LaunchedEffect(chatUserId) {
        viewModel.loadMessages(chatUserId)
        // Llama a la función de tu ViewModel que actualiza Firebase
        viewModel.markMessagesAsRead(chatUserId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Color.White,
                    titleContentColor = Color(0xFF1E88E5)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state    = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message       = message,
                        currentUserId = currentUid
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value         = messageInput,
                    onValueChange = { viewModel.onMessageInputChanged(it) },
                    placeholder   = { Text("Escribe un mensaje...") },
                    modifier      = Modifier.weight(1f),
                    shape         = MaterialTheme.shapes.large,
                    maxLines      = 4
                )
                IconButton(
                    onClick  = { viewModel.sendMessage(chatUserId) },
                    enabled  = messageInput.isNotBlank()
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint               = if (messageInput.isNotBlank()) BubbleMe else Color.Gray
                    )
                }
            }
        }
    }
}

// ── Burbuja ───────────────────────────────────────────────────────────────────
@Composable
fun MessageBubble(message: ChatMessage, currentUserId: String) {
    val isMine = currentUserId.isNotEmpty() && message.senderId == currentUserId

    val timeText = remember(message.timestamp) {
        if (message.timestamp > 0)
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        else ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isMine) 56.dp else 0.dp,
                end   = if (isMine) 0.dp  else 56.dp
            ),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = if (isMine) BubbleMe else BubbleOther,
                shape = RoundedCornerShape(
                    topStart    = 16.dp,
                    topEnd      = 16.dp,
                    bottomStart = if (isMine) 16.dp else 4.dp,
                    bottomEnd   = if (isMine) 4.dp  else 16.dp
                ),
                shadowElevation = 1.dp
            ) {
                Text(
                    text     = message.content,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color    = if (isMine) TextMe else TextOther,
                    fontSize = 15.sp
                )
            }

            if (timeText.isNotEmpty()) {
                // AQUÍ ESTÁ EL CAMBIO 2: Fila para agrupar la hora y los checks
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text     = timeText,
                        fontSize = 10.sp,
                        color    = Color.Gray
                    )

                    // Si el mensaje es mío, muestro los checks
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            // message.isRead debe existir en tu modelo ChatMessage
                            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = if (message.isRead) "Leído" else "Enviado",
                            modifier = Modifier.size(14.dp),
                            // Azul si lo ha leído, gris si solo está enviado
                            tint = if (message.isRead) Color(0xFF1E88E5) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    ChatListScreen(rememberNavController())
}