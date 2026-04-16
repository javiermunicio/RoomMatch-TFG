package com.example.roommatch_pmdm.presentation.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.roommatch_pmdm.R
import com.example.roommatch_pmdm.presentation.navigation.Screen
import com.example.roommatch_pmdm.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val username = viewModel.username.collectAsState()
    val password = viewModel.password.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()
    val loginSuccess = viewModel.loginSuccess.collectAsState()
    val errorMessage = viewModel.errorMessage.collectAsState()

    // Navegar a Home si login exitoso
    if (loginSuccess.value) {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8D5E8))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo y título
            Image(
                painter = painterResource(id = R.drawable.logo_login_roommatch),
                contentDescription = "Imagen de ejemplo"
            )
        Text(
            text = "Encuentra a tu compañero de piso ideal",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF1E88E5),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo Usuario
        OutlinedTextField(
            value = username.value,
            onValueChange = { viewModel.onUsernameChanged(it) },
            label = { Text("Usuario") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFF1E88E5),
                focusedBorderColor = Color(0xFF1E88E5)
            )
        )

        // Campo Contraseña
        OutlinedTextField(
            value = password.value,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFF1E88E5),
                focusedBorderColor = Color(0xFF1E88E5)
            )
        )

        // Mensaje de error
        if (errorMessage.value != null) {
            Text(
                text = errorMessage.value!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Botón Iniciar Sesión
        Button(
            onClick = { viewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E88E5)
            ),
            shape = MaterialTheme.shapes.extraLarge,
            enabled = !isLoading.value
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Iniciar Sesión", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Link a registro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("¿No tienes cuenta? ", color = Color(0xFF1E88E5))
            TextButton(
                onClick = { navController.navigate(Screen.Register.route) }
            ) {
                Text("Regístrate", color = Color(0xFF1E88E5))
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(rememberNavController())
}
