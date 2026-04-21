package com.example.roommatch_pmdm.data.repositories

import com.example.roommatch_pmdm.domain.model.RoomPost
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RoomPostRepository(private val firestore: FirebaseFirestore) {

    private val collection = firestore.collection("roomPosts")

    fun listAll(): Flow<List<RoomPost>> = callbackFlow {
        val listener = collection
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val items = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(RoomPost::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun listByOwner(ownerId: String): Flow<List<RoomPost>> = callbackFlow {
        val listener = collection
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val items = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(RoomPost::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun save(roomPost: RoomPost): Boolean {
        return try {
            collection.add(roomPost).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun delete(id: String): Boolean {
        return try {
            collection.document(id).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}