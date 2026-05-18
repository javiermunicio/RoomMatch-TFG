package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.domain.model.ChatMessage
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.ChatDetailViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.ifEmpty

private val BubbleMe    = Color(0xFF1E88E5)
private val BubbleOther @Composable get() = MaterialTheme.colorScheme.surfaceVariant
private val TextMe @Composable get() = MaterialTheme.colorScheme.onPrimary
private val TextOther @Composable get() = MaterialTheme.colorScheme.onSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatUserId: String,
    navController: NavController? = null,
    viewModel: ChatDetailViewModel = koinViewModel()
) {
    val messages         by viewModel.messages.collectAsState()
    val messageInput     by viewModel.messageInput.collectAsState()
    val currentUid       by viewModel.currentUserIdFlow.collectAsState()
    val listState        = rememberLazyListState()
    val otherUser        by viewModel.otherUser.collectAsState()
    val isBlocked        by viewModel.isBlocked.collectAsState()
    val isBlockedByOther by viewModel.isBlockedByOther.collectAsState()
    val actionDone       by viewModel.actionDone.collectAsState()

    var showMenu          by remember { mutableStateOf(false) }
    var showDeleteDialog  by remember { mutableStateOf(false) }
    var showBlockDialog   by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }

    LaunchedEffect(chatUserId) {
        viewModel.onChatOpened()
        viewModel.loadMessages(chatUserId)
        viewModel.markMessagesAsRead(chatUserId)
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.onChatClosed() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    LaunchedEffect(actionDone) {
        if (actionDone != null) {
            viewModel.clearActionDone()
            navController?.popBackStack()
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Borrar conversación") },
            text  = { Text("¿Borrar este chat? El otro usuario podrá escribirte de nuevo.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteConversation(chatUserId) {}
                }) { Text("Borrar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text("Bloquear usuario") },
            text  = { Text("¿Bloquear a ${otherUser?.username}? No podrá contactarte ni verás su perfil. El chat se borrará.") },
            confirmButton = {
                TextButton(onClick = {
                    showBlockDialog = false
                    viewModel.blockUser(chatUserId) {}
                }) { Text("Bloquear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) { Text("Cancelar") }
            }
        )
    }
    if (showUnblockDialog) {
        AlertDialog(
            onDismissRequest = { showUnblockDialog = false },
            title = { Text("Desbloquear usuario") },
            text  = { Text("¿Desbloquear a ${otherUser?.username}?") },
            confirmButton = {
                TextButton(onClick = {
                    showUnblockDialog = false
                    viewModel.unblockUser(chatUserId)
                }) { Text("Desbloquear") }
            },
            dismissButton = {
                TextButton(onClick = { showUnblockDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier              = Modifier.clickable {
                            navController?.navigate(Screen.InterestedUserProfile.createRoute(chatUserId))
                        }
                    ) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape    = CircleShape,
                            color    = MaterialTheme.colorScheme.outline
                        ) {
                            AsyncImage(
                                model              = otherUser?.profileImage
                                    ?.ifEmpty { "https://via.placeholder.com/38" }
                                    ?: "https://via.placeholder.com/38",
                                contentDescription = null,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        }
                        Column {
                            Text(
                                text       = otherUser?.username ?: "Chat",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 16.sp
                            )
                            if (isBlocked) {
                                Text(
                                    "Bloqueado",
                                    fontSize = 11.sp,
                                    color    = MaterialTheme.colorScheme.error
                                )
                            } else if (isBlockedByOther) {
                                Text(
                                    "No puedes enviar mensajes",
                                    fontSize = 11.sp,
                                    color    = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(
                        expanded         = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text        = { Text("Borrar conversación") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = { showMenu = false; showDeleteDialog = true }
                        )
                        if (isBlocked) {
                            DropdownMenuItem(
                                text        = { Text("Desbloquear usuario") },
                                leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) },
                                onClick     = { showMenu = false; showUnblockDialog = true }
                            )
                        } else {
                            DropdownMenuItem(
                                text        = { Text("Bloquear usuario", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Block,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = { showMenu = false; showBlockDialog = true }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
                    MessageBubble(message = message, currentUserId = currentUid)
                }
            }

            HorizontalDivider()

            when {
                isBlocked -> Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color    = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        "Has bloqueado a este usuario",
                        modifier  = Modifier.padding(16.dp),
                        color     = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        style     = MaterialTheme.typography.bodyMedium
                    )
                }

                isBlockedByOther -> Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color    = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        "No puedes enviar mensajes a este usuario",
                        modifier  = Modifier.padding(16.dp),
                        color     = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        style     = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> Row(
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
                        onClick = { viewModel.sendMessage(chatUserId) },
                        enabled = messageInput.isNotBlank()
                    ) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint               = if (messageInput.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

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
        Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text  = timeText,
                        fontSize = 10.sp,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector        = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = if (message.isRead) "Leído" else "Enviado",
                            modifier           = Modifier.size(14.dp),
                            tint               = if (message.isRead)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}