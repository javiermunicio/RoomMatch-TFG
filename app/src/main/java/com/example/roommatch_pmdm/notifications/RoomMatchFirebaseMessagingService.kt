package com.example.roommatch_pmdm.notifications

import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RoomMatchFirebaseMessagingService : FirebaseMessagingService() {
    private val authRepository: AuthRepository by inject()
    private val userRepository: UserRepository by inject()
    override fun onNewToken(token: String) {
        val userId = authRepository.currentUser?.uid ?: return
        GlobalScope.launch(Dispatchers.IO) {
            userRepository.updateFcmToken(userId, token)
        }
    }
}