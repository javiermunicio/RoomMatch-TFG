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

    private suspend fun getDeletedAtForUser(convId: String, userId: String): Long {
        return try {
            val snap = messagesCollection.document(convId).get().await()
            @Suppress("UNCHECKED_CAST")
            val deletedFor = snap.get("deletedFor") as? Map<String, Long> ?: emptyMap()
            deletedFor[userId] ?: 0L
        } catch (e: Exception) { 0L }
    }

    fun getMessages(currentUserId: String, otherUserId: String): Flow<List<ChatMessage>> =
        callbackFlow {
            val convId = conversationId(currentUserId, otherUserId)
            val deletedAtForMe = getDeletedAtForUser(convId, currentUserId)

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
                    }?.filter { it.timestamp > deletedAtForMe }
                        ?.sortedBy { it.timestamp } ?: emptyList()

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
    suspend fun getLastMessage(currentUserId: String, otherUserId: String): ChatMessage? {
        val convId = conversationId(currentUserId, otherUserId)
        val deletedAtForMe = getDeletedAtForUser(convId, currentUserId)
        val snap = messagesCollection.document(convId).collection("msgs")
            .orderBy("timestamp", FirestoreQuery.Direction.DESCENDING)
            .get().await()
        return snap.documents.mapNotNull {
            it.toObject(ChatMessage::class.java)
        }.filter { it.timestamp > deletedAtForMe }
            .maxByOrNull { it.timestamp }
    }
    fun getLastMessageFlow(currentUserId: String, otherUserId: String): Flow<ChatMessage?> =
        callbackFlow {
            val convId = conversationId(currentUserId, otherUserId)
            val deletedAtForMe = getDeletedAtForUser(convId, currentUserId)
            val listener = messagesCollection.document(convId).collection("msgs")
                .orderBy("timestamp", FirestoreQuery.Direction.DESCENDING)
                .addSnapshotListener { snap, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    val msg = snap?.documents?.mapNotNull {
                        it.toObject(ChatMessage::class.java)
                    }?.filter { it.timestamp > deletedAtForMe }
                        ?.maxByOrNull { it.timestamp }
                    trySend(msg)
                }
            awaitClose { listener.remove() }
        }
    fun getUnreadCountFlow(currentUserId: String, otherUserId: String): Flow<Int> =
        callbackFlow {
            val convId = conversationId(currentUserId, otherUserId)
            val deletedAtForMe = getDeletedAtForUser(convId, currentUserId)
            val listener = messagesCollection.document(convId).collection("msgs")
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener { snap, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    val count = snap?.documents?.mapNotNull {
                        it.toObject(ChatMessage::class.java)
                    }?.count { it.timestamp > deletedAtForMe } ?: 0
                    trySend(count)
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
                val otherUserId = participants.firstOrNull { it != currentUserId }
                    ?: return@mapNotNull null

                @Suppress("UNCHECKED_CAST")
                val deletedFor = doc.get("deletedFor") as? Map<String, Long> ?: emptyMap()
                val deletedAt = deletedFor[currentUserId]
                if (deletedAt != null) {
                    val hasNewMessages = try {
                        val msgs = doc.reference.collection("msgs")
                            .orderBy("timestamp", FirestoreQuery.Direction.DESCENDING)
                            .limit(1)
                            .get().await()
                        val lastTimestamp = msgs.documents.firstOrNull()
                            ?.toObject(ChatMessage::class.java)?.timestamp ?: 0L
                        lastTimestamp > deletedAt
                    } catch (e: Exception) { false }

                    if (hasNewMessages) otherUserId else null
                } else {
                    otherUserId
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun deleteConversation(currentUserId: String, otherUserId: String) {
        try {
            val convId  = conversationId(currentUserId, otherUserId)
            val convRef = messagesCollection.document(convId)

            val convSnap = convRef.get().await()
            if (!convSnap.exists()) return

            @Suppress("UNCHECKED_CAST")
            val deletedFor = convSnap.get("deletedFor") as? Map<String, Long> ?: emptyMap()
            val otherAlsoDeleted = deletedFor.containsKey(otherUserId)

            if (otherAlsoDeleted) {
                val msgs = convRef.collection("msgs").get().await()
                val batch = firestore.batch()
                for (doc in msgs.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
                convRef.delete().await()
            } else {
                convRef.update(
                    "deletedFor.$currentUserId", System.currentTimeMillis()
                ).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}