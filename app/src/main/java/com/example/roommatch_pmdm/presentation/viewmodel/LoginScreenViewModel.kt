package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.roommatch_pmdm.presentation.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginScreenViewModel : ViewModel(){
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun setUsername(username: String) {
        _username.value = username
    }
    fun setPassword(password: String) {
        _password.value = password
    }

    fun clear() {
        _username.value = ""
        _password.value = ""
    }

    fun login(navController: NavController) {
        val isValid = username.value.isNotBlank() && password.value.isNotBlank()
        if (isValid) {
            navController.navigate(Screen.Main.route)
        }
    }

    fun register(navController: NavController) {
        navController.navigate(Screen.Register.route)
    }
}