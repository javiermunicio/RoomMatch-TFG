package com.example.roommatch_pmdm.domain.model

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false
)

data class ChatConversation(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0
)

data class ChatUser(
    val id: String = "",
    val username: String = "",
    val profileImage: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false
)

