package com.example.roommatch_pmdm.data.repositories

import com.example.roommatch_pmdm.domain.model.ChatMessage
import com.example.roommatch_pmdm.domain.model.ChatUser
import com.example.roommatch_pmdm.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(private val firestore: FirebaseFirestore) {

    private val messagesCollection = firestore.collection("messages")
    private val usersCollection    = firestore.collection("users")

    // ID de conversación determinista (siempre el mismo para los dos usuarios)
    fun conversationId(uid1: String, uid2: String): String =
        if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"

    // Escucha mensajes en tiempo real
    fun getMessages(currentUserId: String, otherUserId: String): Flow<List<ChatMessage>> =
        callbackFlow {
            val convId = conversationId(currentUserId, otherUserId)
            val listener = messagesCollection
                .document(convId)
                .collection("msgs")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snap, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    val msgs = snap?.documents?.mapNotNull {
                        it.toObject(ChatMessage::class.java)
                    } ?: emptyList()
                    trySend(msgs)
                }
            awaitClose { listener.remove() }
        }

    // Envía un mensaje
    suspend fun sendMessage(currentUserId: String, otherUserId: String, content: String) {
        val convId = conversationId(currentUserId, otherUserId)
        val msg = ChatMessage(
            id          = System.currentTimeMillis().toString(),
            senderId    = currentUserId,
            recipientId = otherUserId,
            content     = content,
            timestamp   = System.currentTimeMillis(),
            isRead      = false
        )
        messagesCollection.document(convId).collection("msgs")
            .document(msg.id).set(msg).await()
    }

    // Obtiene los datos de usuario para mostrar en la lista de chats
    suspend fun getUserData(userId: String): User? {
        return usersCollection.document(userId).get().await()
            .toObject(User::class.java)
    }

    // Obtiene el último mensaje de una conversación
    suspend fun getLastMessage(currentUserId: String, otherUserId: String): ChatMessage? {
        val convId = conversationId(currentUserId, otherUserId)
        val snap = messagesCollection.document(convId).collection("msgs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get().await()
        return snap.documents.firstOrNull()?.toObject(ChatMessage::class.java)
    }


    suspend fun markMessagesAsRead(currentUserId: String, otherUserId: String) {
        try {
            val convId = conversationId(currentUserId, otherUserId)
            val msgsRef = messagesCollection.document(convId).collection("msgs")

            // Usamos recipientId en lugar de whereNotEqualTo para evitar bloqueos de Firebase
            val unreadQuery = msgsRef
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            for (doc in unreadQuery.documents) {
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}