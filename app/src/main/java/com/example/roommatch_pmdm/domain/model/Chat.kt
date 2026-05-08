package com.example.roommatch_pmdm.domain.model

import com.google.firebase.firestore.PropertyName // Añade este import arriba
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val recipientId: String = "", // Asumo que añadiste esto por mi mensaje anterior
    val content: String = "",
    val timestamp: Long = 0L,

    // Obligamos a Firebase a usar "isRead" exactamente
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false
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
    val isRead: Boolean = false,
    // UID de quien envió el último mensaje (para saber si fui yo o el otro)
    val lastMessageSenderId: String = ""
)