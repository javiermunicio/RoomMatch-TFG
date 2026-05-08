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

private val FigmaPink       = Color(0xFFF8C8C8)
private val FigmaBlue       = Color(0xFF4A90D9)
private val FigmaRed        = Color(0xFFE05A5A)
private val FigmaButtonBlue = Color(0xFF4A9FD9)
private val WhiteCard       = Color(0xFFFFFFFF)

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

        // ── Fondo rosa superior ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.42f)
                .background(FigmaPink)
        )

        // ── Fondo blanco inferior ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.58f)
                .align(Alignment.BottomCenter)
                .background(WhiteCard)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(48.dp))

            // ── Logo en círculo blanco ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(WhiteCard),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(id = R.drawable.logo_login_roommatch),
                    contentDescription = "RoomMatch logo",
                    modifier           = Modifier.size(110.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── "RoomMatch" bicolor ───────────────────────────────────────────
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = FigmaBlue)) { append("Room") }
                    withStyle(SpanStyle(color = FigmaRed))  { append("Match") }
                },
                fontSize   = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Card blanca con formulario ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(WhiteCard)
                    .padding(horizontal = 28.dp, vertical = 28.dp)
            ) {
                Column {

                    // Usuario
                    FigmaField(
                        value         = username,
                        onValueChange = { viewModel.onUsernameChanged(it) },
                        label         = "Usuario",
                        icon          = Icons.Outlined.Person
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Correo
                    FigmaField(
                        value         = email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label         = "Correo Electrónico",
                        icon          = Icons.Outlined.Email,
                        keyboardType  = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contraseña
                    FigmaField(
                        value            = password,
                        onValueChange    = { viewModel.onPasswordChanged(it) },
                        label            = "Contraseña",
                        icon             = Icons.Outlined.Lock,
                        isPassword       = true,
                        passwordVisible  = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Confirmar contraseña
                    FigmaField(
                        value            = confirmPassword,
                        onValueChange    = { viewModel.onConfirmPasswordChanged(it) },
                        label            = "Confirmar Contraseña",
                        icon             = Icons.Outlined.Lock,
                        isPassword       = true,
                        passwordVisible  = confirmPasswordVisible,
                        onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible }
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
                            color    = FigmaBlue,
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