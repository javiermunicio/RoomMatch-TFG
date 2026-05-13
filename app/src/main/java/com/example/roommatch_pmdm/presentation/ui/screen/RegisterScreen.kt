package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.roommatch_pmdm.R
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.RegisterViewModel
import org.koin.androidx.compose.koinViewModel
import com.example.roommatch_pmdm.ui.theme.FigmaBlue
import com.example.roommatch_pmdm.ui.theme.FigmaRed
import com.example.roommatch_pmdm.ui.theme.FigmaButtonBlue

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val username        by viewModel.username.collectAsState()
    val email           by viewModel.email.collectAsState()
    val password        by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val isLoading       by viewModel.isLoading.collectAsState()
    val registerSuccess by viewModel.registerSuccess.collectAsState()
    val errorMessage    by viewModel.errorMessage.collectAsState()

    // Colores que se adaptan al tema
    val topBgColor  = MaterialTheme.colorScheme.primaryContainer
    val cardBgColor = MaterialTheme.colorScheme.surface
    val fieldBorder = MaterialTheme.colorScheme.primary
    val labelColor  = MaterialTheme.colorScheme.primary

    var passwordVisible        by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    if (registerSuccess) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.Onboarding.route) {
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Fondo superior adaptativo ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.42f)
                .background(topBgColor)
        )

        // ── Fondo inferior adaptativo ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.58f)
                .align(Alignment.BottomCenter)
                .background(cardBgColor)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(48.dp))

            // ── Logo ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(cardBgColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(id = R.drawable.logo_login_roommatch),
                    contentDescription = "RoomMatch logo",
                    modifier           = Modifier.size(110.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── "RoomMatch" bicolor ───────────────────────────────────────
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = FigmaBlue)) { append("Room") }
                    withStyle(SpanStyle(color = FigmaRed))  { append("Match") }
                },
                fontSize   = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Card blanca/oscura con formulario ─────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(cardBgColor)
                    .padding(horizontal = 28.dp, vertical = 28.dp)
            ) {
                Column {

                    // Usuario
                    AdaptiveField(
                        value         = username,
                        onValueChange = { viewModel.onUsernameChanged(it) },
                        label         = "Usuario",
                        icon          = Icons.Outlined.Person,
                        fieldBorder   = fieldBorder,
                        labelColor    = labelColor,
                        cardBgColor   = cardBgColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Correo
                    AdaptiveField(
                        value         = email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label         = "Correo Electrónico",
                        icon          = Icons.Outlined.Email,
                        keyboardType  = KeyboardType.Email,
                        fieldBorder   = fieldBorder,
                        labelColor    = labelColor,
                        cardBgColor   = cardBgColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contraseña
                    AdaptiveField(
                        value            = password,
                        onValueChange    = { viewModel.onPasswordChanged(it) },
                        label            = "Contraseña",
                        icon             = Icons.Outlined.Lock,
                        isPassword       = true,
                        passwordVisible  = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible },
                        fieldBorder      = fieldBorder,
                        labelColor       = labelColor,
                        cardBgColor      = cardBgColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Confirmar contraseña
                    AdaptiveField(
                        value            = confirmPassword,
                        onValueChange    = { viewModel.onConfirmPasswordChanged(it) },
                        label            = "Confirmar Contraseña",
                        icon             = Icons.Outlined.Lock,
                        isPassword       = true,
                        passwordVisible  = confirmPasswordVisible,
                        onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible },
                        fieldBorder      = fieldBorder,
                        labelColor       = labelColor,
                        cardBgColor      = cardBgColor
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter   = fadeIn() + expandVertically(),
                        exit    = fadeOut() + shrinkVertically()
                    ) {
                        errorMessage?.let {
                            Text(
                                text     = it,
                                color    = FigmaRed,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }

                    // ¿Ya tienes cuenta?
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ya tienes cuenta? ",
                            color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color          = FigmaBlue,
                                        fontWeight     = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) { append("Inicia Sesión") }
                            },
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { navController.popBackStack() }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Crear Cuenta
                    Button(
                        onClick  = { viewModel.register() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape  = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = FigmaButtonBlue,
                            disabledContainerColor = FigmaButtonBlue.copy(alpha = 0.5f)
                        ),
                        enabled = !isLoading
                    ) {
                        AnimatedContent(
                            targetState = isLoading,
                            transitionSpec = {
                                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                            },
                            label = "registerBtn"
                        ) { loading ->
                            if (loading) {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(20.dp),
                                        color       = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        "Creando cuenta…",
                                        color      = Color.White,
                                        fontSize   = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Text(
                                    "Crear Cuenta",
                                    color      = Color.White,
                                    fontSize   = 17.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(rememberNavController())
}