package com.example.roommatch_pmdm.presentation.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.ChatRepository
import com.example.roommatch_pmdm.data.repositories.MatchRepository
import com.example.roommatch_pmdm.domain.model.ChatMessage
import com.example.roommatch_pmdm.domain.model.ChatUser
import com.example.roommatch_pmdm.notifications.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ─── ChatListViewModel ───────────────────────────────────────────────────────

class ChatListViewModel(
    private val matchRepository: MatchRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chatUsers = MutableStateFlow<List<ChatUser>>(emptyList())
    val chatUsers: StateFlow<List<ChatUser>> = _chatUsers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { loadChats() }

    fun loadChats() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val matchedIds = matchRepository.getMatchedUserIds(currentUserId).toSet()
                val activeIds = chatRepository.getActiveConversationUserIds(currentUserId).toSet()
                val allUserIds = (matchedIds + activeIds).toList()

                _chatUsers.value = allUserIds.mapNotNull { userId ->
                    val user = chatRepository.getUserData(userId)
                    val lastMsg = chatRepository.getLastMessage(currentUserId, userId)
                    ChatUser(
                        id           = userId,
                        username     = user?.username?.ifEmpty { user.email } ?: userId,
                        profileImage = user?.profileImage ?: "",
                        lastMessage  = lastMsg?.content ?: "Toca para chatear",
                        timestamp    = lastMsg?.timestamp ?: 0L,
                        isRead       = lastMsg?.isRead ?: true
                    )
                }.sortedByDescending { it.timestamp }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() { loadChats() }
}

// ─── ChatDetailViewModel ─────────────────────────────────────────────────────

class ChatDetailViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput

    private val _currentUserIdFlow = MutableStateFlow(
        authRepository.currentUser?.uid ?: ""
    )
    val currentUserIdFlow: StateFlow<String> = _currentUserIdFlow

    private val _otherUser = MutableStateFlow<ChatUser?>(null)
    val otherUser: StateFlow<ChatUser?> = _otherUser

    private var messagesJob: Job? = null
    private var currentOtherUserId: String? = null

    fun onMessageInputChanged(text: String) {
        _messageInput.value = text
    }

    fun loadMessages(otherUserId: String) {
        // Evitar recargar si ya estamos escuchando este mismo chat
        if (otherUserId == currentOtherUserId && messagesJob?.isActive == true) return

        val uid = authRepository.currentUser?.uid ?: return
        _currentUserIdFlow.value = uid
        currentOtherUserId = otherUserId

        // Cargar datos del otro usuario
        viewModelScope.launch {
            val user = chatRepository.getUserData(otherUserId)
            _otherUser.value = ChatUser(
                id           = otherUserId,
                username     = user?.username?.ifEmpty { user.email } ?: otherUserId,
                profileImage = user?.profileImage ?: ""
            )
        }

        // Cancelar listener anterior y crear uno nuevo
        messagesJob?.cancel()
        _messages.value = emptyList()

        messagesJob = viewModelScope.launch {
            chatRepository.getMessages(uid, otherUserId).collect { msgs ->
                val sorted = msgs.sortedBy { it.timestamp }

                // Detectar mensajes nuevos para notificación
                val previousIds = _messages.value.map { it.id }.toSet()
                val newIncoming = sorted.filter { it.id !in previousIds && it.senderId != uid }

                if (_messages.value.isNotEmpty() && newIncoming.isNotEmpty()) {
                    val canNotify = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else true

                    if (canNotify) {
                        newIncoming.forEach { msg ->
                            NotificationHelper.showChatNotification(
                                context    = context,
                                senderName = _otherUser.value?.username ?: "Nuevo mensaje",
                                message    = msg.content
                            )
                        }
                    }
                }

                _messages.value = sorted
            }
        }
    }

    fun sendMessage(otherUserId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        val content = _messageInput.value.trim()
        if (content.isEmpty()) return

        // Limpiar input ANTES de enviar para feedback inmediato
        _messageInput.value = ""

        viewModelScope.launch {
            try {
                chatRepository.sendMessage(uid, otherUserId, content)
            } catch (e: Exception) {
                // Si falla, restaurar el texto para que el usuario pueda reintentar
                _messageInput.value = content
                e.printStackTrace()
            }
        }
    }

    fun markMessagesAsRead(otherUserId: String) {
        val currentUid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(currentUid, otherUserId)
        }
    }

    fun resetState() {
        messagesJob?.cancel()
        messagesJob = null
        currentOtherUserId = null
        _messages.value = emptyList()
        _messageInput.value = ""
        _otherUser.value = null
    }

    override fun onCleared() {
        super.onCleared()
        messagesJob?.cancel()
    }
}