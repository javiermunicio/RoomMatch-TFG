package com.example.roommatch_pmdm.notifications

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.android.ext.android.inject

class NotificationListenerService : Service() {

    private val authRepository: AuthRepository by inject()
    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isListening = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isListening) {
            startListening()
        }
        return START_STICKY
    }

    private fun startListening() {
        val uid = authRepository.currentUser?.uid ?: return
        listenerRegistration?.remove()
        listenerRegistration = firestore
            .collection("notifications")
            .document(uid)
            .collection("pending")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                for (change in snapshots.documentChanges) {
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val data  = change.document.data
                        val title = data["title"] as? String ?: continue
                        val body  = data["body"]  as? String ?: continue

                        showNotificationIfAllowed(title, body)

                        serviceScope.launch {
                            try {
                                change.document.reference.delete().await()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

        isListening = true
    }

    private fun showNotificationIfAllowed(title: String, body: String) {
        val canNotify = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        if (!canNotify) return

        when {
            title.contains("mensaje", ignoreCase = true) ->
                NotificationHelper.showChatNotification(this, title, body)
            title.contains("match", ignoreCase = true) ->
                NotificationHelper.showMatchNotification(this, body)
            else ->
                NotificationHelper.showInterestNotification(this, title, body)
        }
    }

    override fun onDestroy() {
        listenerRegistration?.remove()
        listenerRegistration = null
        isListening = false
        serviceScope.cancel()
        super.onDestroy()
    }
}