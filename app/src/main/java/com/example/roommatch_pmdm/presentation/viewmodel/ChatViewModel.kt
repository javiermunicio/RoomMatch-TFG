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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

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
                // Obtener todos los usuarios con los que hay conversación
                val matchedIds = matchRepository.getMatchedUserIds(currentUserId).toSet()
                val activeIds  = chatRepository.getActiveConversationUserIds(currentUserId).toSet()
                val allUserIds = (matchedIds + activeIds).toList()

                // Para cada usuario, construir un Flow reactivo del último mensaje
                // y combinarlo en una lista actualizada en tiempo real
                if (allUserIds.isEmpty()) {
                    _isLoading.value = false
                    return@launch
                }

                // Creamos un flow por usuario y los combinamos
                val flows = allUserIds.map { userId ->
                    chatRepository.getLastMessageFlow(currentUserId, userId)
                        .combine(
                            chatRepository.getUnreadCountFlow(currentUserId, userId)
                        ) { lastMsg, unreadCount ->
                            Triple(userId, lastMsg, unreadCount)
                        }
                }

                // Combinamos todos los flows en uno solo
                kotlinx.coroutines.flow.combine(flows) { triples ->
                    triples.mapNotNull { (userId, lastMsg, unreadCount) ->
                        val user = chatRepository.getUserData(userId) ?: return@mapNotNull null
                        ChatUser(
                            id                   = userId,
                            username             = user.username.ifEmpty { user.email },
                            profileImage         = user.profileImage,
                            lastMessage          = lastMsg?.content ?: "Toca para chatear",
                            timestamp            = lastMsg?.timestamp ?: 0L,
                            // isRead desde perspectiva del receptor (yo)
                            // Si el último mensaje me lo enviaron a mí y no está leído → false
                            isRead               = if (lastMsg?.recipientId == currentUserId)
                                lastMsg.isRead
                            else true,
                            lastMessageSenderId  = lastMsg?.senderId ?: ""
                        )
                    }.sortedByDescending { it.timestamp }
                }.collect { users ->
                    _chatUsers.value = users
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    fun refresh() { loadChats() }
}

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

    fun onMessageInputChanged(text: String) {
        _messageInput.value = text
    }

    fun loadMessages(otherUserId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        _currentUserIdFlow.value = uid

        viewModelScope.launch {
            val user = chatRepository.getUserData(otherUserId)
            _otherUser.value = ChatUser(
                id           = otherUserId,
                username     = user?.username?.ifEmpty { user.email } ?: otherUserId,
                profileImage = user?.profileImage ?: ""
            )
        }

        messagesJob?.cancel()
        _messages.value = emptyList()

        messagesJob = viewModelScope.launch {
            chatRepository.getMessages(uid, otherUserId).collect { msgs ->
                val sorted = msgs.sortedBy { it.timestamp }

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
        _messageInput.value = ""
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(uid, otherUserId, content)
            } catch (e: Exception) {
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

    override fun onCleared() {
        super.onCleared()
        messagesJob?.cancel()
    }
}