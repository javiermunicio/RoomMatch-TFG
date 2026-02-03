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
import com.example.roommatch_pmdm.presentation.viewmodel.LoginScreenViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController,
                loginScreenViewModel: LoginScreenViewModel = viewModel()) {
    val username by loginScreenViewModel.username.collectAsState()
    val password by loginScreenViewModel.password.collectAsState()
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
                    onValueChange = { loginScreenViewModel.setUsername(it) },
                    label = {
                        Text("Username")
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = password,
                    onValueChange = { loginScreenViewModel.setPassword(it) },
                    label = {
                        Text("Password")
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(
                        enabled = loginEnabled,
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Login Valido, Entrando")
                            }
                            loginScreenViewModel.login(navController)
                        }
                    ) {
                        Text("Login")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Iniciando Registro")
                            }
                            loginScreenViewModel.register(navController)
                        }
                    ) {
                        Text("Register")
                    }
                }

            }

        }
    )
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(rememberNavController())
}
