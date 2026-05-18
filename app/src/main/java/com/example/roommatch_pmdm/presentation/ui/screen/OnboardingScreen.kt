package com.example.roommatch_pmdm.presentation.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.OnboardingViewModel
import com.example.roommatch_pmdm.ui.theme.RoomBlue
import com.example.roommatch_pmdm.ui.theme.RoomBlueSoft
import org.koin.androidx.compose.koinViewModel

private val stepTitles = listOf(
    "Tu identidad",
    "Tu situación",
    "Tu personalidad"
)
private val stepSubtitles = listOf(
    "Cuéntanos cómo te llamas y añade una foto",
    "Edad, ciudad, bio y presupuesto",
    "¿Cómo eres como compañero/a de piso?"
)
private val stepIcons = listOf(
    Icons.Filled.Person,
    Icons.Filled.Home,
    Icons.Filled.Favorite
)

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val step         by viewModel.step.collectAsState()
    val isDone       by viewModel.isDone.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val stepError    by viewModel.stepError.collectAsState()

    LaunchedEffect(isDone) {
        if (isDone) navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Onboarding.route) { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.38f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            RoomBlue.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(48.dp))

            StepProgressHeader(currentStep = step, totalSteps = 3)

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInHorizontally { it / 4 })
                        .togetherWith(fadeOut(tween(200)) + slideOutHorizontally { -it / 4 })
                },
                label = "stepTitle"
            ) { s ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape    = CircleShape,
                        color    = RoomBlue.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector        = stepIcons[s - 1],
                                contentDescription = null,
                                tint               = RoomBlue,
                                modifier           = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text       = stepTitles[s - 1],
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = RoomBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text      = stepSubtitles[s - 1],
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier        = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape           = MaterialTheme.shapes.large,
                color           = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        (fadeIn(tween(280))).togetherWith(fadeOut(tween(180)))
                    },
                    label = "stepContent"
                ) { s ->
                    Column(modifier = Modifier.padding(20.dp)) {
                        when (s) {
                            1 -> StepOne(viewModel)
                            2 -> StepTwo(viewModel)
                            3 -> StepThree(viewModel)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = (stepError ?: errorMessage) != null,
                enter   = fadeIn() + expandVertically(),
                exit    = fadeOut() + shrinkVertically()
            ) {
                (stepError ?: errorMessage)?.let { err ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        color    = MaterialTheme.colorScheme.errorContainer,
                        shape    = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier              = Modifier.padding(12.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.error,
                                modifier           = Modifier.size(16.dp)
                            )
                            Text(
                                err,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(
                    visible = step > 1,
                    enter   = fadeIn() + expandHorizontally(),
                    exit    = fadeOut() + shrinkHorizontally()
                ) {
                    OutlinedButton(
                        onClick  = { viewModel.prevStep() },
                        modifier = Modifier.height(52.dp),
                        shape    = MaterialTheme.shapes.extraLarge,
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Atrás", fontWeight = FontWeight.Medium)
                    }
                }

                Button(
                    onClick  = { if (step == 3) viewModel.finish() else viewModel.nextStep() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape  = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(containerColor = RoomBlue),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            if (step == 3) "¡Listo!" else "Continuar",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            if (step == 3) Icons.Filled.Check else Icons.Filled.ArrowForward,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            ) {
                Text(
                    "Configurar más tarde",
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StepProgressHeader(currentStep: Int, totalSteps: Int) {
    Row(
        modifier          = Modifier.padding(horizontal = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..totalSteps).forEach { i ->
            val isCompleted = i < currentStep
            val isCurrent   = i == currentStep

            Surface(
                modifier = Modifier.size(if (isCurrent) 36.dp else 30.dp),
                shape    = CircleShape,
                color    = when {
                    isCompleted -> RoomBlue
                    isCurrent   -> RoomBlue
                    else        -> MaterialTheme.colorScheme.surfaceVariant
                },
                shadowElevation = if (isCurrent) 4.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCompleted) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint     = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text       = "$i",
                            color      = if (isCurrent) Color.White
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp
                        )
                    }
                }
            }
            if (i < totalSteps) {
                val lineColor by animateColorAsState(
                    targetValue   = if (i < currentStep) RoomBlue else MaterialTheme.colorScheme.outlineVariant,
                    animationSpec = tween(400),
                    label         = "lineColor$i"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(lineColor)
                )
            }
        }
    }
}

@Composable
private fun OnboardingField(
    value:         String,
    onValueChange: (String) -> Unit,
    label:         String,
    icon:          ImageVector,
    modifier:      Modifier     = Modifier.fillMaxWidth(),
    keyboardType:  KeyboardType = KeyboardType.Text,
    minLines:      Int          = 1,
    placeholder:   String       = ""
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = modifier,
        label         = { Text(label, fontSize = 12.sp) },
        placeholder   = if (placeholder.isNotEmpty()) ({
            Text(placeholder, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        }) else null,
        leadingIcon   = {
            Icon(
                icon,
                contentDescription = null,
                tint     = if (value.isNotBlank()) RoomBlue
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        },
        minLines        = minLines,
        singleLine      = minLines == 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape           = MaterialTheme.shapes.medium,
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = RoomBlue,
            unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            focusedLabelColor       = RoomBlue,
            focusedContainerColor   = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun StepOne(viewModel: OnboardingViewModel) {
    val username        by viewModel.username.collectAsState()
    val profileImageUrl by viewModel.profileImageUrl.collectAsState()
    val isUploading     by viewModel.isUploadingImage.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.uploadImage(it) } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                RoomBlue.copy(alpha = 0.6f),
                                RoomBlue.copy(alpha = 0.1f),
                                RoomBlue.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(3.dp, RoomBlue, CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model              = profileImageUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(alpha = if (profileImageUrl.isEmpty()) 0.15f else 0.35f)
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint     = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            if (profileImageUrl.isEmpty()) {
                                Text(
                                    "Añadir foto",
                                    fontSize   = 10.sp,
                                    color      = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text      = if (profileImageUrl.isNotEmpty()) "Toca para cambiar la foto"
            else "La foto ayuda a generar confianza",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            textAlign = TextAlign.Center
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        OnboardingField(
            value         = username,
            onValueChange = { viewModel.onUsernameChanged(it) },
            label         = "¿Cómo te llamas?",
            placeholder   = "Tu nombre de usuario",
            icon          = Icons.Filled.Badge
        )
    }
}

@Composable
private fun StepTwo(viewModel: OnboardingViewModel) {
    val age    by viewModel.age.collectAsState()
    val city   by viewModel.city.collectAsState()
    val bio    by viewModel.bio.collectAsState()
    val budget by viewModel.budget.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OnboardingField(
                value         = age,
                onValueChange = { viewModel.onAgeChanged(it) },
                label         = "Edad",
                placeholder   = "18",
                icon          = Icons.Filled.Cake,
                keyboardType  = KeyboardType.Number,
                modifier      = Modifier.weight(1f)
            )
            OnboardingField(
                value         = city,
                onValueChange = { viewModel.onCityChanged(it) },
                label         = "Ciudad",
                placeholder   = "Madrid",
                icon          = Icons.Filled.LocationOn,
                modifier      = Modifier.weight(1.6f)
            )
        }

        OnboardingField(
            value         = budget,
            onValueChange = { viewModel.onBudgetChanged(it) },
            label         = "Presupuesto máximo (€/mes)",
            placeholder   = "600",
            icon          = Icons.Filled.EuroSymbol,
            keyboardType  = KeyboardType.Number
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color    = RoomBlueSoft,
            shape    = MaterialTheme.shapes.small
        ) {
            Row(
                modifier              = Modifier.padding(10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint     = RoomBlue,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Te mostraremos pisos dentro de tu rango y personas con presupuesto similar",
                    style  = MaterialTheme.typography.bodySmall,
                    color  = RoomBlue,
                    lineHeight = 17.sp
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        OnboardingField(
            value         = bio,
            onValueChange = { viewModel.onBioChanged(it) },
            label         = "Cuéntanos algo sobre ti",
            placeholder   = "Soy estudiante, me gusta el orden...",
            icon          = Icons.Filled.Notes,
            minLines      = 3
        )
    }
}

@Composable
private fun StepThree(viewModel: OnboardingViewModel) {
    val selectedHabits by viewModel.selectedHabits.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                "Selecciona los que te describan",
                style  = MaterialTheme.typography.bodySmall,
                color  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
            AnimatedContent(
                targetState = selectedHabits.size,
                transitionSpec = {
                    fadeIn(tween(150)) togetherWith fadeOut(tween(100))
                },
                label = "habitCount"
            ) { count ->
                Surface(
                    color = if (count > 0) RoomBlue else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ) {
                    Text(
                        text       = "$count",
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        color      = if (count > 0) Color.White
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 12.sp
                    )
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp),
            modifier              = Modifier.fillMaxWidth()
        ) {
            viewModel.availableHabits.forEach { habit ->
                val selected = habit in selectedHabits
                HabitChip(
                    label    = habit,
                    selected = selected,
                    onClick  = { viewModel.toggleHabit(habit) }
                )
            }
        }
        if (selectedHabits.isEmpty()) {
            Text(
                "Selecciona al menos uno para continuar",
                style  = MaterialTheme.typography.labelSmall,
                color  = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HabitChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue   = if (selected) RoomBlue else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(200),
        label         = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue   = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label         = "chipText"
    )
    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "chipScale"
    )

    Surface(
        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
        color    = bgColor,
        shape    = MaterialTheme.shapes.extraLarge,
        shadowElevation = if (selected) 3.dp else 0.dp
    ) {
        Row(
            modifier              = Modifier
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint     = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                label,
                color      = textColor,
                fontSize   = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}