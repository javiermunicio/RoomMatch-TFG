package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.roommatch_pmdm.presentation.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RegisterScreenViewModel : ViewModel(){
    private val _username = MutableStateFlow("")
    val username : StateFlow<String> = _username
    private val _password = MutableStateFlow("")
    val password : StateFlow<String> = _password
    private val _email = MutableStateFlow("")
    val email : StateFlow<String> = _email

    fun setUsername(username: String) {
        _username.value = username
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun clear() {
        _username.value = ""
        _password.value = ""
        _email.value = ""
    }

    fun register(navController: NavController) {
        val isValid = email.value.isNotBlank() && password.value.isNotBlank()
        if (isValid) {
            navController.navigate(Screen.Main.route)
        }
    }
}
