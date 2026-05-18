package com.example.roommatch_pmdm.data.remote

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class StorageRepository(private val context: Context) {

    private val cloudName   = "dkybceppl"
    private val uploadPreset = "Imagenes-RoomMatch"

    suspend fun uploadProfileImage(imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val stream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext Result.failure(Exception("No se pudo leer la imagen"))
            val bytes = stream.readBytes()
            stream.close()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", "profile.jpg",
                    bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .addFormDataPart("upload_preset", uploadPreset)
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(requestBody)
                .build()

            val response = OkHttpClient().newCall(request).execute()
            val body = response.body?.string()
            if (!response.isSuccessful) {
                println("ERROR CLOUDINARY: Código ${response.code} - Body: $body")
                return@withContext Result.failure(Exception("Error de Cloudinary: $body"))
            }
            if (body.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Respuesta vacía"))
            }
            val url = JSONObject(body).getString("secure_url")
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}