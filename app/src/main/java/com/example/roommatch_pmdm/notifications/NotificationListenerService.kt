package com.example.roommatch_pmdm.notifications

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
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
    private val TAG = "NotifListenerService"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundCompat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startListening()
        return START_STICKY  // Android reinicia el servicio si lo mata
    }

    private fun startForegroundCompat() {
        val notification = NotificationHelper.buildSilentForegroundNotification(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                FOREGROUND_NOTIF_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(FOREGROUND_NOTIF_ID, notification)
        }
        Log.d(TAG, "Foreground service iniciado")
    }

    private fun startListening() {
        val uid = authRepository.currentUser?.uid ?: run {
            Log.w(TAG, "Usuario no autenticado, deteniendo servicio")
            stopSelf()
            return
        }

        listenerRegistration?.remove()

        listenerRegistration = firestore
            .collection("notifications")
            .document(uid)
            .collection("pending")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Error escuchando notificaciones", error)
                    return@addSnapshotListener
                }
                if (snapshots == null) return@addSnapshotListener

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
                                Log.e(TAG, "Error borrando notificación pending", e)
                            }
                        }
                    }
                }
            }

        Log.d(TAG, "Escuchando notificaciones para $uid")
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartIntent = Intent(applicationContext, NotificationListenerService::class.java)
        startService(restartIntent)
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        listenerRegistration?.remove()
        listenerRegistration = null
        serviceScope.cancel()
        Log.d(TAG, "Servicio destruido")
        super.onDestroy()
    }

    companion object {
        private const val FOREGROUND_NOTIF_ID = 9001
    }
}