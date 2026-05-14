package com.example.roommatch_pmdm.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RoomMatchFirebaseMessagingService : FirebaseMessagingService() {

    private val userRepository: com.example.roommatch_pmdm.data.repositories.UserRepository by inject()
    private val authRepository: com.example.roommatch_pmdm.data.repositories.AuthRepository by inject()

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: return
        val body  = message.notification?.body  ?: return

        val canNotify = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        if (!canNotify) return

        // Diferenciamos el canal por el título para reutilizar NotificationHelper
        when {
            title.contains("mensaje", ignoreCase = true) ->
                NotificationHelper.showChatNotification(this, title, body)
            title.contains("match", ignoreCase = true) ->
                NotificationHelper.showMatchNotification(this, body)
            else ->
                NotificationHelper.showInterestNotification(this, title, body)
        }
    }

    override fun onNewToken(token: String) {
        val userId = authRepository.currentUser?.uid ?: return
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            userRepository.updateFcmToken(userId, token)
        }
    }
}