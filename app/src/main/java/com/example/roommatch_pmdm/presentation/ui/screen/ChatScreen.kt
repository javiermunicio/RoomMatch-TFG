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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
private val BubbleOther @Composable get() = MaterialTheme.colorScheme.surfaceVariant
private val TextMe @Composable get() = MaterialTheme.colorScheme.onPrimary
private val TextOther @Composable get() = MaterialTheme.colorScheme.onSurface

// ── ChatListScreen ────────────────────────────────────────────────────────────
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

    // También refresca en ON_RESUME del lifecycle
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
    // ¿El último mensaje lo envié yo?
    val iSentLast = chatUser.lastMessageSenderId == currentUserId
    // ¿Hay mensajes que no he leído (me los enviaron a mí y no están leídos)?
    val hasUnread = !iSentLast && !chatUser.isRead && chatUser.lastMessage != "Toca para chatear"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        onClick = onItemClick,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasUnread) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                AsyncImage(
                    model = chatUser.profileImage.ifEmpty { "https://via.placeholder.com/56" },
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }

            // Nombre + último mensaje
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    chatUser.username,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (hasUnread) FontWeight.ExtraBold else FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Si yo envié el último, muestro el check de estado del envío
                    if (iSentLast && chatUser.lastMessage != "Toca para chatear") {
                        Icon(
                            imageVector = if (chatUser.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (chatUser.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                    Text(
                        chatUser.lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }

            // Columna derecha: hora + badge de no leído
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (chatUser.timestamp > 0) {
                    val timeText = remember(chatUser.timestamp) {
                        val now = System.currentTimeMillis()
                        val diff = now - chatUser.timestamp
                        when {
                            diff < 60 * 60 * 1000 -> // menos de 1h → HH:mm
                                java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date(chatUser.timestamp))
                            diff < 24 * 60 * 60 * 1000 -> // menos de 24h → "Ayer"
                                "Ayer"
                            else -> // más de 24h → dd/MM
                                java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
                                    .format(java.util.Date(chatUser.timestamp))
                        }
                    }
                    Text(
                        timeText,
                        fontSize = 11.sp,
                        color = if (hasUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                if (hasUnread) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {}
                }
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
    val otherUser by viewModel.otherUser.collectAsState()

    LaunchedEffect(chatUserId) {
        viewModel.loadMessages(chatUserId)
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
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape    = CircleShape,
                            color = MaterialTheme.colorScheme.outline
                        ) {
                            AsyncImage(
                                model              = otherUser?.profileImage
                                    ?.ifEmpty { "https://via.placeholder.com/38" }
                                    ?: "https://via.placeholder.com/38",
                                contentDescription = null,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        Text(
                            text       = otherUser?.username ?: "Chat",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                    }
                },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
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
                    .background(MaterialTheme.colorScheme.background)
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
                    .background(MaterialTheme.colorScheme.surface)
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
                        tint               = if (messageInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                            tint = if (message.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
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