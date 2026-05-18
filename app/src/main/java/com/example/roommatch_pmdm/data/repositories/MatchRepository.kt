package com.example.roommatch_pmdm.data.repositories

import com.example.roommatch_pmdm.domain.model.Like
import com.example.roommatch_pmdm.domain.model.Match
import com.example.roommatch_pmdm.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MatchRepository(private val firestore: FirebaseFirestore) {

    private val likesCollection  = firestore.collection("likes")
    private val matchesCollection = firestore.collection("matches")
    private val usersCollection  = firestore.collection("users")

    suspend fun saveLikeAndCheckMatch(fromUserId: String, toUserId: String): Boolean {
        val likeId = "${fromUserId}_${toUserId}"
        likesCollection.document(likeId).set(
            Like(fromUserId = fromUserId, toUserId = toUserId, createdAt = System.currentTimeMillis())
        ).await()

        val reverseId = "${toUserId}_${fromUserId}"
        val reverseDoc = likesCollection.document(reverseId).get().await()
        return if (reverseDoc.exists()) {
            val matchId = if (fromUserId < toUserId) "${fromUserId}_${toUserId}"
            else "${toUserId}_${fromUserId}"
            matchesCollection.document(matchId).set(
                Match(
                    id       = matchId,
                    userId1  = fromUserId,
                    userId2  = toUserId,
                    matchedAt = System.currentTimeMillis()
                )
            ).await()
            true
        } else {
            false
        }
    }
    fun getMatches(currentUserId: String): Flow<List<String>> = callbackFlow {
        val fromUser1 = mutableSetOf<String>()
        val fromUser2 = mutableSetOf<String>()

        val listener1 = matchesCollection
            .whereEqualTo("userId1", currentUserId)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                fromUser1.clear()
                snap?.documents?.forEach { doc ->
                    doc.toObject(Match::class.java)?.let { fromUser1.add(it.userId2) }
                }
                trySend((fromUser1 + fromUser2).toList())
            }

        val listener2 = matchesCollection
            .whereEqualTo("userId2", currentUserId)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                fromUser2.clear()
                snap?.documents?.forEach { doc ->
                    doc.toObject(Match::class.java)?.let { fromUser2.add(it.userId1) }
                }
                trySend((fromUser1 + fromUser2).toList())
            }

        awaitClose {
            listener1.remove()
            listener2.remove()
        }
    }
    suspend fun getMatchedUserIds(currentUserId: String): List<String> {
        val result = mutableListOf<String>()

        val snap1 = matchesCollection.whereEqualTo("userId1", currentUserId).get().await()
        snap1.documents.forEach { doc ->
            doc.toObject(Match::class.java)?.let { result.add(it.userId2) }
        }

        val snap2 = matchesCollection.whereEqualTo("userId2", currentUserId).get().await()
        snap2.documents.forEach { doc ->
            doc.toObject(Match::class.java)?.let { result.add(it.userId1) }
        }

        return result
    }
    suspend fun getUsersToSwipe(currentUserId: String): List<User> {
        val likedSnap = likesCollection
            .whereEqualTo("fromUserId", currentUserId).get().await()
        val alreadyLiked = likedSnap.documents.map { it.toObject(Like::class.java)!!.toUserId }.toSet()
        val usersSnap = usersCollection.get().await()
        return usersSnap.documents.mapNotNull { doc ->
            val user = doc.toObject(User::class.java) ?: return@mapNotNull null
            if (user.id == currentUserId || user.id in alreadyLiked) null else user
        }
    }

    suspend fun hasAlreadyLiked(fromUserId: String, toUserId: String): Boolean {
        val likeId = "${fromUserId}_${toUserId}"
        return likesCollection.document(likeId).get().await().exists()
    }

    suspend fun deleteMatchAndLikes(currentUserId: String, otherUserId: String) {
        try {
            val matchId1 = "${currentUserId}_${otherUserId}"
            val matchId2 = "${otherUserId}_${currentUserId}"
            val likeId1  = "${currentUserId}_${otherUserId}"
            val likeId2  = "${otherUserId}_${currentUserId}"

            matchesCollection.document(matchId1).delete().await()
            matchesCollection.document(matchId2).delete().await()
            likesCollection.document(likeId1).delete().await()
            likesCollection.document(likeId2).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}