package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository          // ← añadido
) : ViewModel() {

    private val _username        = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _email           = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password        = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _isLoading       = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess

    private val _errorMessage    = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun onUsernameChanged(v: String)        { _username.value = v }
    fun onEmailChanged(v: String)           { _email.value = v }
    fun onPasswordChanged(v: String)        { _password.value = v }
    fun onConfirmPasswordChanged(v: String) { _confirmPassword.value = v }

    fun register() {
        viewModelScope.launch {
            // Validaciones
            if (_username.value.isBlank() || _email.value.isBlank() ||
                _password.value.isEmpty() || _confirmPassword.value.isEmpty()) {
                _errorMessage.value = "Por favor completa todos los campos"
                return@launch
            }
            if (_password.value != _confirmPassword.value) {
                _errorMessage.value = "Las contraseñas no coinciden"
                return@launch
            }
            if (_password.value.length < 6) {
                _errorMessage.value = "La contraseña debe tener al menos 6 caracteres"
                return@launch
            }

            _isLoading.value = true

            // 1. Crear usuario en Firebase Auth
            val authResult = authRepository.register(_email.value.trim(), _password.value)
            authResult.fold(
                onSuccess = { firebaseUser ->
                    // 2. Guardar perfil en Firestore
                    userRepository.createUserIfNotExists(
                        userId   = firebaseUser.uid,
                        email    = firebaseUser.email ?: _email.value.trim(),
                        username = _username.value.trim()
                    )
                    // No bloqueamos el registro si falla Firestore,
                    // pero sí lo registramos para debug.
                    _registerSuccess.value = true
                    _errorMessage.value    = null
                },
                onFailure = { e ->
                    _errorMessage.value = mapFirebaseError(e.message)
                }
            )

            _isLoading.value = false
        }
    }

    fun clearError() { _errorMessage.value = null }

    private fun mapFirebaseError(message: String?): String = when {
        message == null                                          -> "Error desconocido"
        "email address is already in use" in message.lowercase() -> "Este correo ya está registrado"
        "badly formatted" in message                             -> "El formato del correo no es válido"
        "password" in message.lowercase()                        -> "La contraseña no cumple los requisitos"
        "network" in message.lowercase()                         -> "Error de conexión"
        else                                                     -> "Error al crear la cuenta"
    }
}