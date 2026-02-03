package com.example.roommatch_pmdm.presentation.ui.screen


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.roommatch_pmdm.presentation.viewmodel.RegisterScreenViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController,
                   registerScreenViewModel: RegisterScreenViewModel = viewModel()) {
    val username by registerScreenViewModel.username.collectAsState()
    val password by registerScreenViewModel.password.collectAsState()
    val email by registerScreenViewModel.email.collectAsState()
    val loginEnabled by remember {
        derivedStateOf {
            username.isNotBlank() && password.isNotBlank()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { innerPadding ->
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = username,
                    onValueChange = { registerScreenViewModel.setUsername(it) },
                    label = {
                        Text("Username")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = email,
                    onValueChange = { registerScreenViewModel.setEmail(it) },
                    label = {
                        Text("Email")
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = password,
                    onValueChange = { registerScreenViewModel.setPassword(it) },
                    label = {
                        Text("Password")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    enabled = loginEnabled,
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Iniciando Sesion")
                        }
                        registerScreenViewModel.register(navController)
                    }
                ) {
                    Text("Register")
                }
            }

        }
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(rememberNavController())
}
