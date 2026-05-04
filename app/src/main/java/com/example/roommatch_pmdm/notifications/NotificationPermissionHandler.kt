package com.example.roommatch_pmdm.notifications

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * Composable que pide el permiso POST_NOTIFICATIONS en Android 13+
 * al montarse en pantalla. Llama a [onResult] con true/false.
 */
@Composable
fun RequestNotificationPermission(onResult: (Boolean) -> Unit = {}) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        // Android < 13: no hace falta permiso en tiempo de ejecución
        LaunchedEffect(Unit) { onResult(true) }
        return
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> onResult(granted) }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}