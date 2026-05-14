package com.example.roommatch_pmdm.notifications

import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FcmTokenManager(
    private val userRepository: UserRepository,
    private val messaging: FirebaseMessaging
) {
    suspend fun refreshAndSaveToken(userId: String) {
        try {
            val token = messaging.token.await()
            userRepository.updateFcmToken(userId, token)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}