package com.example.roommatch_pmdm.presentation.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel

private val RoomBlue = Color(0xFF4A90D9)
private val ChipSelected = Color(0xFF4A90D9)

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val step         by viewModel.step.collectAsState()
    val isDone       by viewModel.isDone.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(isDone) {
        if (isDone) navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Onboarding.route) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // ── Fondo adaptado al tema ──────────────────────────────────────
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Configura tu perfil",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = RoomBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Indicador de pasos ───────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..3).forEach { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == step) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == step) RoomBlue
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (step) {
            1 -> StepOne(viewModel)
            2 -> StepTwo(viewModel)
            3 -> StepThree(viewModel)
        }

        val stepError by viewModel.stepError.collectAsState()
        (stepError ?: errorMessage)?.let {
            Text(
                it,
                color    = MaterialTheme.colorScheme.error,
                style    = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (step > 1) {
                OutlinedButton(onClick = { viewModel.prevStep() }) {
                    Text("Atrás")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Button(
                onClick = { if (step == 3) viewModel.finish() else viewModel.nextStep() },
                enabled = !isLoading,
                colors  = ButtonDefaults.buttonColors(containerColor = RoomBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (step == 3) "¡Listo!" else "Siguiente")
                }
            }
        }

        // ── Saltar ───────────────────────────────────────────────────────────
        TextButton(onClick = {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
        }) {
            Text(
                "Saltar por ahora",
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
        }
    }
}

// ── Paso 1: foto + nombre ────────────────────────────────────────────────────

@Composable
private fun StepOne(viewModel: OnboardingViewModel) {
    val username        by viewModel.username.collectAsState()
    val profileImageUrl by viewModel.profileImageUrl.collectAsState()
    val isUploading     by viewModel.isUploadingImage.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.uploadImage(it) } }

    // ── Selector de foto ─────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            // fondo del avatar adaptado al tema
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(3.dp, RoomBlue, CircleShape)
            .clickable { launcher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model              = profileImageUrl.ifEmpty { "https://via.placeholder.com/120" },
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )
        // Overlay de cámara
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(alpha = if (profileImageUrl.isEmpty()) 0.25f else 0.35f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(28.dp),
                    color       = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint     = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value         = username,
        onValueChange = { viewModel.onUsernameChanged(it) },
        label         = { Text("¿Cómo te llamas?") },
        modifier      = Modifier.fillMaxWidth(),
        shape         = MaterialTheme.shapes.large,
        singleLine    = true,
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = RoomBlue,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor    = RoomBlue
        )
    )
}

// ── Paso 2: edad, ciudad, bio ─────────────────────────────────────────────────

@Composable
private fun StepTwo(viewModel: OnboardingViewModel) {
    val age  by viewModel.age.collectAsState()
    val city by viewModel.city.collectAsState()
    val bio  by viewModel.bio.collectAsState()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = RoomBlue,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor    = RoomBlue
    )

    OutlinedTextField(
        value         = age,
        onValueChange = { viewModel.onAgeChanged(it) },
        label         = { Text("Edad") },
        modifier      = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape         = MaterialTheme.shapes.large,
        singleLine    = true,
        colors        = fieldColors
    )
    OutlinedTextField(
        value         = city,
        onValueChange = { viewModel.onCityChanged(it) },
        label         = { Text("Ciudad") },
        modifier      = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape         = MaterialTheme.shapes.large,
        singleLine    = true,
        colors        = fieldColors
    )
    OutlinedTextField(
        value         = bio,
        onValueChange = { viewModel.onBioChanged(it) },
        label         = { Text("Cuéntanos algo sobre ti") },
        modifier      = Modifier.fillMaxWidth(),
        shape         = MaterialTheme.shapes.large,
        minLines      = 3,
        colors        = fieldColors
    )
}

// ── Paso 3: hábitos ───────────────────────────────────────────────────────────

@Composable
private fun StepThree(viewModel: OnboardingViewModel) {
    val selectedHabits by viewModel.selectedHabits.collectAsState()

    Text(
        "¿Cómo eres como compañero/a?",
        style      = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color      = RoomBlue,
        modifier   = Modifier.padding(bottom = 16.dp)
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp),
        modifier              = Modifier.fillMaxWidth()
    ) {
        viewModel.availableHabits.forEach { habit ->
            val selected = habit in selectedHabits
            FilterChip(
                selected = selected,
                onClick  = { viewModel.toggleHabit(habit) },
                label    = { Text(habit) },
                colors   = FilterChipDefaults.filterChipColors(
                    // seleccionado: azul + texto blanco
                    selectedContainerColor = ChipSelected,
                    selectedLabelColor     = Color.White,
                    // no seleccionado: superficie del tema
                    containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled              = true,
                    selected             = selected,
                    borderColor          = MaterialTheme.colorScheme.outline,
                    selectedBorderColor  = ChipSelected
                )
            )
        }
    }
}