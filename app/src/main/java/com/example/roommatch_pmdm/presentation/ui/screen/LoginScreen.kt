package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.roommatch_pmdm.R
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.LoginViewModel
import org.koin.androidx.compose.koinViewModel

// ── Paleta fija de marca (igual en claro y oscuro) ────────────────────────────
private val FigmaBlue       = Color(0xFF4A90D9)
private val FigmaRed        = Color(0xFFE05A5A)
private val FigmaButtonBlue = Color(0xFF4A9FD9)

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = koinViewModel()
) {
    val username     by viewModel.username.collectAsState()
    val password     by viewModel.password.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Colores que se adaptan al tema
    val topBgColor    = MaterialTheme.colorScheme.primaryContainer
    val cardBgColor   = MaterialTheme.colorScheme.surface
    val fieldBorder   = MaterialTheme.colorScheme.primary
    val labelColor    = MaterialTheme.colorScheme.primary

    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Fondo superior (usa primaryContainer del tema) ────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.52f)
                .background(topBgColor)
        )

        // ── Fondo inferior (usa surface del tema) ─────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.48f)
                .align(Alignment.BottomCenter)
                .background(cardBgColor)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(56.dp))

            // ── Logo en círculo ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(55.dp))
                    .background(cardBgColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(id = R.drawable.logo_login_roommatch),
                    contentDescription = "RoomMatch logo",
                    modifier           = Modifier.size(110.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── "RoomMatch" bicolor ───────────────────────────────────────
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = FigmaBlue)) { append("Room") }
                    withStyle(SpanStyle(color = FigmaRed))  { append("Match") }
                },
                fontSize   = 36.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "Encuentra a tu compañero\nde piso ideal",
                fontSize  = 15.sp,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Card con formulario ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(cardBgColor)
                    .padding(horizontal = 28.dp, vertical = 28.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

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

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(24.dp))

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

                    // ¿No tienes cuenta?
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "¿No tienes cuenta? ",
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
                                ) { append("Registrarse") }
                            },
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Register.route)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Iniciar Sesión
                    Button(
                        onClick  = { viewModel.login() },
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
                            label = "loginBtn"
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
                                        "Entrando…",
                                        color      = Color.White,
                                        fontSize   = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Text(
                                    "Iniciar Sesión",
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

// ── Campo adaptativo al tema ──────────────────────────────────────────────────

@Composable
fun AdaptiveField(
    value:            String,
    onValueChange:    (String) -> Unit,
    label:            String,
    icon:             ImageVector,
    keyboardType:     KeyboardType = KeyboardType.Text,
    isPassword:       Boolean = false,
    passwordVisible:  Boolean = false,
    onTogglePassword: () -> Unit = {},
    fieldBorder:      Color,
    labelColor:       Color,
    cardBgColor:      Color
) {
    Column {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier              = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = labelColor,
                modifier           = Modifier.size(18.dp)
            )
            Text(
                text     = label,
                color    = labelColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        OutlinedTextField(
            value             = value,
            onValueChange     = onValueChange,
            modifier          = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape             = RoundedCornerShape(14.dp),
            singleLine        = true,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions   = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon      = if (isPassword) ({
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector        = if (passwordVisible)
                            Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint               = labelColor,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }) else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = fieldBorder,
                unfocusedBorderColor    = fieldBorder,
                focusedContainerColor   = cardBgColor,
                unfocusedContainerColor = cardBgColor,
                cursorColor             = fieldBorder,
                focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor      = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

// Alias de compatibilidad: FigmaField sigue funcionando para RegisterScreen
@Composable
fun FigmaField(
    value:            String,
    onValueChange:    (String) -> Unit,
    label:            String,
    icon:             ImageVector,
    keyboardType:     KeyboardType = KeyboardType.Text,
    isPassword:       Boolean = false,
    passwordVisible:  Boolean = false,
    onTogglePassword: () -> Unit = {}
) {
    val fieldBorder = MaterialTheme.colorScheme.primary
    val labelColor  = MaterialTheme.colorScheme.primary
    val cardBgColor = MaterialTheme.colorScheme.surface

    AdaptiveField(
        value            = value,
        onValueChange    = onValueChange,
        label            = label,
        icon             = icon,
        keyboardType     = keyboardType,
        isPassword       = isPassword,
        passwordVisible  = passwordVisible,
        onTogglePassword = onTogglePassword,
        fieldBorder      = fieldBorder,
        labelColor       = labelColor,
        cardBgColor      = cardBgColor
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(rememberNavController())
}