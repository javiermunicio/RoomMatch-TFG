package com.example.roommatch_pmdm.notifications

import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FcmNotificationSender(
    private val userRepository: UserRepository
) {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun sendToUser(
        targetUserId: String,
        title: String,
        body: String
    ) {
        try {
            val notification = mapOf(
                "title"     to title,
                "body"      to body,
                "timestamp" to System.currentTimeMillis(),
                "read"      to false
            )
            firestore
                .collection("notifications")
                .document(targetUserId)
                .collection("pending")
                .add(notification)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}