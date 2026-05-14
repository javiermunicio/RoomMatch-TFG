package com.example.roommatch_pmdm.domain.usecase

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.roommatch_pmdm.data.repositories.MatchRepository
import com.example.roommatch_pmdm.notifications.NotificationHelper

class SaveLikeAndCheckMatchUseCase(
    private val matchRepository: MatchRepository,
    private val context: Context
) {
    suspend operator fun invoke(
        currentUserId: String,
        targetUserId: String,
        targetUsername: String
    ): Boolean {
        val isMatch = matchRepository.saveLikeAndCheckMatch(currentUserId, targetUserId)
        if (isMatch) notifyIfAllowed(targetUsername)
        return isMatch
    }

    private fun notifyIfAllowed(matchedUsername: String) {
        val canNotify = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        if (canNotify) {
            NotificationHelper.showMatchNotification(
                context         = context,
                matchedUsername = matchedUsername
            )
        }
    }
}