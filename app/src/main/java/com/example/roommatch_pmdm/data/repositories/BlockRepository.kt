package com.example.roommatch_pmdm.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BlockRepository(private val firestore: FirebaseFirestore) {

    private val collection = firestore.collection("blocks")

    suspend fun blockUser(currentUserId: String, blockedUserId: String): Result<Unit> {
        return try {
            val id = "${currentUserId}_${blockedUserId}"
            collection.document(id).set(
                mapOf(
                    "blockerId" to currentUserId,
                    "blockedId" to blockedUserId,
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unblockUser(currentUserId: String, blockedUserId: String): Result<Unit> {
        return try {
            val id = "${currentUserId}_${blockedUserId}"
            collection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isBlocked(currentUserId: String, otherUserId: String): Boolean {
        return try {
            val id = "${currentUserId}_${otherUserId}"
            collection.document(id).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isBlockedByOther(currentUserId: String, otherUserId: String): Boolean {
        return try {
            val id = "${otherUserId}_${currentUserId}"
            collection.document(id).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getBlockedUserIds(currentUserId: String): List<String> {
        return try {
            val snap = collection
                .whereEqualTo("blockerId", currentUserId)
                .get().await()
            snap.documents.mapNotNull { it.getString("blockedId") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUsersWhoBlockedMe(currentUserId: String): List<String> {
        return try {
            val snap = collection
                .whereEqualTo("blockedId", currentUserId)
                .get().await()
            snap.documents.mapNotNull { it.getString("blockerId") }
        } catch (e: Exception) {
            emptyList()
        }
    }
}