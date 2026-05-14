package com.example.roommatch_pmdm.notifications

import com.example.roommatch_pmdm.data.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class FcmNotificationSender(
    private val userRepository: UserRepository
) {
    // Usa la FCM HTTP v1 API a través de un servidor propio o Firebase Functions
    // Para este proyecto usamos la Legacy API con Server Key
    private val serverKey = "TU_SERVER_KEY" // obtenla en Firebase Console → Project Settings → Cloud Messaging

    suspend fun sendToUser(
        targetUserId: String,
        title: String,
        body: String
    ) = withContext(Dispatchers.IO) {
        try {
            val token = userRepository.getUser(targetUserId).getOrNull()?.fcmToken
                ?: return@withContext

            val json = JSONObject().apply {
                put("to", token)
                put("notification", JSONObject().apply {
                    put("title", title)
                    put("body", body)
                })
            }

            val request = Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .addHeader("Authorization", "key=$serverKey")
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}