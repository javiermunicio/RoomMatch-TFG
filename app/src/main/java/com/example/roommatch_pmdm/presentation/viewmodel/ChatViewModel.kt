package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.ChatRepository
import com.example.roommatch_pmdm.data.repositories.MatchRepository
import com.example.roommatch_pmdm.domain.model.ChatMessage
import com.example.roommatch_pmdm.domain.model.ChatUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─── ChatListViewModel ───────────────────────────────────────────────────────

class ChatListViewModel(
    private val matchRepository: MatchRepository,
    private val authRepository:  AuthRepository,
    private val chatRepository:  ChatRepository
) : ViewModel() {

    private val _chatUsers = MutableStateFlow<List<ChatUser>>(emptyList())
    val chatUsers: StateFlow<List<ChatUser>> = _chatUsers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { loadChats() }

    private fun loadChats() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Recarga cada vez que se abre la pantalla — simple y fiable
                val matchedIds = matchRepository.getMatchedUserIds(currentUserId)
                _chatUsers.value = matchedIds.mapNotNull { userId ->
                    val user    = chatRepository.getUserData(userId)
                    val lastMsg = chatRepository.getLastMessage(currentUserId, userId)
                    ChatUser(
                        id           = userId,
                        username     = user?.username?.ifEmpty { user.email } ?: userId,
                        profileImage = user?.profileImage ?: "",
                        lastMessage  = lastMsg?.content ?: "Toca para chatear",
                        timestamp    = lastMsg?.timestamp ?: 0L,
                        isRead       = lastMsg?.isRead ?: true
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun refresh() {
        loadChats()
    }
}

// ─── ChatDetailViewModel ─────────────────────────────────────────────────────

// ─── ChatDetailViewModel ─────────────────────────────────────────────────────

class ChatDetailViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentUserIdFlow = MutableStateFlow(
        authRepository.currentUser?.uid ?: ""
    )
    val currentUserIdFlow: StateFlow<String> = _currentUserIdFlow

    val currentUserId: String? get() = authRepository.currentUser?.uid

    fun onMessageInputChanged(text: String) { _messageInput.value = text }

    fun loadMessages(otherUserId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        _currentUserIdFlow.value = uid
        viewModelScope.launch {
            chatRepository.getMessages(uid, otherUserId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun sendMessage(otherUserId: String) {
        val uid     = authRepository.currentUser?.uid ?: return
        val content = _messageInput.value.trim()
        if (content.isEmpty()) return
        viewModelScope.launch {
            chatRepository.sendMessage(uid, otherUserId, content)
            _messageInput.value = ""
        }
    }

    fun markMessagesAsRead(otherUserId: String) {
        val currentUid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            // Le pasamos el trabajo al repositorio
            chatRepository.markMessagesAsRead(currentUid, otherUserId)
        }
    }
}