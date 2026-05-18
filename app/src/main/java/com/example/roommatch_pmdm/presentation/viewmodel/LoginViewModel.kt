// En LoginViewModel.kt — añadir el Context al constructor y arrancar el servicio

package com.example.roommatch_pmdm.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.notifications.FcmTokenManager
import com.example.roommatch_pmdm.notifications.NotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val fcmTokenManager: FcmTokenManager,
    private val context: Context
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun onUsernameChanged(newUsername: String) { _username.value = newUsername }
    fun onPasswordChanged(newPassword: String) { _password.value = newPassword }

    fun login() {
        viewModelScope.launch {
            if (_username.value.isEmpty() || _password.value.isEmpty()) {
                _errorMessage.value = "Por favor completa todos los campos"
                return@launch
            }
            _isLoading.value = true
            val result = authRepository.login(_username.value.trim(), _password.value)
            result.fold(
                onSuccess = { user ->
                    fcmTokenManager.refreshAndSaveToken(user.uid)

                    context.startService(
                        Intent(context, NotificationListenerService::class.java)
                    )

                    _loginSuccess.value = true
                    _errorMessage.value = null
                },
                onFailure = { e ->
                    _errorMessage.value = mapFirebaseError(e.message)
                    _loginSuccess.value = false
                }
            )
            _isLoading.value = false
        }
    }

    fun clearError() { _errorMessage.value = null }

    private fun mapFirebaseError(message: String?): String = when {
        message == null -> "Error desconocido"
        "password" in message -> "Contraseña incorrecta"
        "no user" in message.lowercase() -> "No existe una cuenta con ese correo"
        "badly formatted" in message -> "El formato del correo no es válido"
        "network" in message.lowercase() -> "Error de conexión"
        else -> "Error al iniciar sesión"
    }
}