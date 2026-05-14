package com.example.roommatch_pmdm.domain.usecase

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.roommatch_pmdm.data.repositories.InterestRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.model.Interest
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.example.roommatch_pmdm.notifications.FcmNotificationSender
import com.example.roommatch_pmdm.notifications.NotificationHelper

class ToggleInterestUseCase(
    private val interestRepository: InterestRepository,
    private val userRepository: UserRepository,
    private val fcmNotificationSender: FcmNotificationSender,
    private val context: Context
) {
    suspend operator fun invoke(
        currentUserId: String,
        post: RoomPost,
        isCurrentlyInterested: Boolean
    ): Result<Boolean> {
        return if (isCurrentlyInterested) {
            interestRepository.removeInterest(currentUserId, post.id).map { false }
        } else {
            val username = userRepository.getUser(currentUserId).getOrNull()?.username ?: "Usuario"
            val interest = Interest(
                postId             = post.id,
                postOwnerId        = post.ownerId,
                interestedUserId   = currentUserId,
                interestedUsername = username,
                createdAt          = System.currentTimeMillis()
            )
            interestRepository.addInterest(interest).map {
                notifyLocalIfAllowed(username, post.title)
                fcmNotificationSender.sendToUser(
                    targetUserId = post.ownerId,
                    title        = "Nuevo interesado en tu anuncio",
                    body         = "$username está interesado en \"${post.title}\""
                )
                true
            }
        }
    }

    private fun notifyLocalIfAllowed(username: String, postTitle: String) {
        val canNotify = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        if (canNotify) {
            NotificationHelper.showInterestNotification(
                context            = context,
                interestedUsername = username,
                postTitle          = postTitle
            )
        }
    }
}