package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun onUsernameChanged(newUsername: String) {
        _username.value = newUsername
    }

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
    }

    fun onConfirmPasswordChanged(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    fun register() {
        viewModelScope.launch {
            if (username.value.isEmpty() || email.value.isEmpty() ||
                password.value.isEmpty() || confirmPassword.value.isEmpty()) {
                _errorMessage.value = "Por favor completa todos los campos"
                return@launch
            }

            if (password.value != confirmPassword.value) {
                _errorMessage.value = "Las contraseñas no coinciden"
                return@launch
            }

            if (!email.value.contains("@")) {
                _errorMessage.value = "El email no es válido"
                return@launch
            }

            _isLoading.value = true

            try {
                // TODO: Integrar con Firebase Authentication
                _registerSuccess.value = true
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _registerSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
