package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.domain.model.ChatMessage
import com.example.roommatch_pmdm.domain.model.ChatUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatListViewModel : ViewModel() {

    private val _chatUsers = MutableStateFlow<List<ChatUser>>(emptyList())
    val chatUsers: StateFlow<List<ChatUser>> = _chatUsers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Cargar desde Firebase
                _chatUsers.value = generateMockChats()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateMockChats(): List<ChatUser> {
        return listOf(
            ChatUser(
                id = "1",
                username = "nebulanomod",
                profileImage = "",
                lastMessage = "Has compartido una publicación...",
                timestamp = System.currentTimeMillis() - 7200000,
                isRead = true
            ),
            ChatUser(
                id = "2",
                username = "pepitowdwd",
                profileImage = "",
                lastMessage = "Ha compartido una publicación...",
                timestamp = System.currentTimeMillis() - 25200000,
                isRead = false
            )
        )
    }
}

class ChatDetailViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun onMessageInputChanged(newMessage: String) {
        _messageInput.value = newMessage
    }

    fun sendMessage(recipientId: String) {
        viewModelScope.launch {
            if (_messageInput.value.isNotEmpty()) {
                val newMessage = ChatMessage(
                    id = System.currentTimeMillis().toString(),
                    senderId = "currentUserId",
                    recipientId = recipientId,
                    content = _messageInput.value,
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )

                // TODO: Guardar en Firebase
                _messages.value = _messages.value + newMessage
                _messageInput.value = ""
            }
        }
    }

    fun loadMessages(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Cargar desde Firebase
                _messages.value = generateMockMessages()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateMockMessages(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = "1",
                senderId = "other",
                recipientId = "self",
                content = "¡Hola! ¿Cómo estás?",
                timestamp = System.currentTimeMillis() - 3600000,
                isRead = true
            )
        )
    }
}
