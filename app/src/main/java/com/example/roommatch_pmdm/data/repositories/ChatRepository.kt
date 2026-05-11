package com.example.roommatch_pmdm.data.repositories

import com.example.roommatch_pmdm.domain.model.ChatMessage
import com.example.roommatch_pmdm.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query as FirestoreQuery
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(private val firestore: FirebaseFirestore) {

    private val messagesCollection = firestore.collection("messages")
    private val usersCollection    = firestore.collection("users")

    fun conversationId(uid1: String, uid2: String): String =
        if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"

    fun getMessages(currentUserId: String, otherUserId: String): Flow<List<ChatMessage>> =
        callbackFlow {
            val convId = conversationId(currentUserId, otherUserId)
            val listener = messagesCollection
                .document(convId)
                .collection("msgs")
                .orderBy("timestamp", FirestoreQuery.Direction.ASCENDING)
                .addSnapshotListener { snap, error ->
                    if (error != null) {
                        println("Error getMessages: ${error.message}")
                        return@addSnapshotListener
                    }
                    val msgs = snap?.documents?.mapNotNull {
                        it.toObject(ChatMessage::class.java)
                    }?.sortedBy { it.timestamp } ?: emptyList()

                    trySend(msgs)
                }
            awaitClose { listener.remove() }
        }

    suspend fun sendMessage(currentUserId: String, otherUserId: String, content: String) {
        val convId  = conversationId(currentUserId, otherUserId)
        val convRef = messagesCollection.document(convId)

        val convSnap = convRef.get().await()
        if (!convSnap.exists()) {
            convRef.set(
                mapOf("participants" to listOf(currentUserId, otherUserId))
            ).await()
        }

        val msg = ChatMessage(
            id          = System.currentTimeMillis().toString(),
            senderId    = currentUserId,
            recipientId = otherUserId,
            content     = content,
            timestamp   = System.currentTimeMillis(),
            isRead      = false
        )
        convRef.collection("msgs").document(msg.id).set(msg).await()
    }

    suspend fun getUserData(userId: String): User? =
        usersCollection.document(userId).get().await().toObject(User::class.java)

    // Obtiene el último mensaje de una conversación (one-shot)
    suspend fun getLastMessage(currentUserId: String, otherUserId: String): ChatMessage? {
        val convId = conversationId(currentUserId, otherUserId)
        val snap = messagesCollection.document(convId).collection("msgs")
            .orderBy("timestamp", FirestoreQuery.Direction.DESCENDING)
            .limit(1)
            .get().await()
        return snap.documents.mapNotNull {
            it.toObject(ChatMessage::class.java)
        }.maxByOrNull { it.timestamp }
    }

    // Escucha en tiempo real el último mensaje de una conversación
    fun getLastMessageFlow(currentUserId: String, otherUserId: String): Flow<ChatMessage?> =
        callbackFlow {
            val convId = conversationId(currentUserId, otherUserId)
            val listener = messagesCollection.document(convId).collection("msgs")
                .orderBy("timestamp", FirestoreQuery.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snap, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    val msg = snap?.documents?.firstOrNull()?.toObject(ChatMessage::class.java)
                    trySend(msg)
                }
            awaitClose { listener.remove() }
        }

    // Cuenta mensajes no leídos que me enviaron
    fun getUnreadCountFlow(currentUserId: String, otherUserId: String): Flow<Int> =
        callbackFlow {
            val convId = conversationId(currentUserId, otherUserId)
            val listener = messagesCollection.document(convId).collection("msgs")
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener { snap, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    trySend(snap?.size() ?: 0)
                }
            awaitClose { listener.remove() }
        }

    suspend fun markMessagesAsRead(currentUserId: String, otherUserId: String) {
        try {
            val convId = conversationId(currentUserId, otherUserId)
            val unread = messagesCollection.document(convId).collection("msgs")
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("isRead", false)
                .get().await()

            val batch = firestore.batch()
            for (doc in unread.documents) {
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getActiveConversationUserIds(currentUserId: String): List<String> {
        return try {
            val snap = messagesCollection
                .whereArrayContains("participants", currentUserId)
                .get()
                .await()

            snap.documents.mapNotNull { doc ->
                @Suppress("UNCHECKED_CAST")
                val participants = doc.get("participants") as? List<String>
                    ?: return@mapNotNull null
                participants.firstOrNull { it != currentUserId }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun deleteConversation(currentUserId: String, otherUserId: String) {
        try {
            val convId = conversationId(currentUserId, otherUserId)
            val convRef = messagesCollection.document(convId)

            // Borrar todos los mensajes
            val msgs = convRef.collection("msgs").get().await()
            val batch = firestore.batch()
            for (doc in msgs.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()

            // Borrar el documento de la conversación
            convRef.delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}