package com.example.roommatch_pmdm.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.roommatch_pmdm.MainActivity
import com.example.roommatch_pmdm.R

object NotificationHelper {
    const val CHANNEL_CHAT    = "channel_chat"
    const val CHANNEL_MATCH   = "channel_match"
    const val CHANNEL_INTEREST = "channel_interest"
    const val CHANNEL_SILENT = "channel_silent"

    private const val NOTIF_CHAT     = 1001
    private const val NOTIF_MATCH    = 1002
    private const val NOTIF_INTEREST = 1003


    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_CHAT,
                    "Mensajes",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones de nuevos mensajes de chat"
                    enableVibration(true)
                }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_MATCH,
                    "Matches",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones cuando haces match con alguien"
                    enableVibration(true)
                }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_SILENT,
                    "Servicio en segundo plano",
                    NotificationManager.IMPORTANCE_MIN  // sin sonido, sin icono en statusbar
                ).apply {
                    description = "Mantiene las notificaciones activas en segundo plano"
                    setShowBadge(false)
                    setSound(null, null)
                    enableVibration(false)
                }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_INTEREST,
                    "Interesados",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones cuando alguien muestra interés en tu anuncio"
                }
            )
        }
    }
    private fun mainPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showChatNotification(
        context: Context,
        senderName: String,
        message: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_CHAT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Nuevo mensaje de $senderName")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent(context))
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIF_CHAT, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showMatchNotification(
        context: Context,
        matchedUsername: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_MATCH)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("¡Nuevo Match! 🎉")
            .setContentText("¡Has hecho match con $matchedUsername!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent(context))
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIF_MATCH, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showInterestNotification(
        context: Context,
        interestedUsername: String,
        postTitle: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_INTEREST)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Nuevo interesado en tu anuncio")
            .setContentText("$interestedUsername está interesado en \"$postTitle\"")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent(context))
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIF_INTEREST, notification)
    }
    fun buildSilentForegroundNotification(context: Context): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_SILENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("RoomMatch activo")
            .setContentText("Recibirás notificaciones en tiempo real")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setContentIntent(mainPendingIntent(context))
            .build()
    }

}