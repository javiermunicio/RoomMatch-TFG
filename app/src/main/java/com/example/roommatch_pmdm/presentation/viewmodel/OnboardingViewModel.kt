package com.example.roommatch_pmdm.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.remote.StorageRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    val step = MutableStateFlow(1) // 1, 2 o 3
    val stepError = MutableStateFlow<String?>(null)

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _age = MutableStateFlow("")
    val age: StateFlow<String> = _age

    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city

    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio

    private val _selectedHabits = MutableStateFlow<List<String>>(emptyList())
    val selectedHabits: StateFlow<List<String>> = _selectedHabits

    private val _profileImageUrl = MutableStateFlow("")
    val profileImageUrl: StateFlow<String> = _profileImageUrl


    val isLoading = MutableStateFlow(false)
    val isUploadingImage = MutableStateFlow(false)
    val isDone = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    val availableHabits = listOf(
        "Madrugador", "Noctámbulo", "Ordenado", "Tranquilo",
        "Sociable", "Deportista", "Fumador", "No fumador",
        "Con mascotas", "Vegetariano", "Músico", "Gamer"
    )

    fun onUsernameChanged(v: String) {
        _username.value = v
    }

    fun onAgeChanged(v: String) {
        _age.value = v
    }

    fun onCityChanged(v: String) {
        _city.value = v
    }

    fun onBioChanged(v: String) {
        _bio.value = v
    }


    fun toggleHabit(habit: String) {
        _selectedHabits.value = if (habit in _selectedHabits.value)
            _selectedHabits.value - habit
        else
            _selectedHabits.value + habit
    }


    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            isUploadingImage.value = true
            storageRepository.uploadProfileImage(uri).fold(
                onSuccess = { _profileImageUrl.value = it },
                onFailure = { errorMessage.value = "Error al subir la imagen" }
            )
            isUploadingImage.value = false
        }
    }

    fun nextStep() {
        when (step.value) {
            1 -> if (_username.value.isBlank()) {
                stepError.value = "El nombre es obligatorio"; return
            }

            2 -> {
                if (_age.value.isBlank()) {
                    stepError.value = "La edad es obligatoria"; return
                }
                val ageInt = _age.value.toIntOrNull()
                if (ageInt == null || ageInt < 18 || ageInt > 99) {
                    stepError.value = "La edad debe estar entre 18 y 99 años"; return
                }
                if (_city.value.isBlank()) {
                    stepError.value = "La ciudad es obligatoria"; return
                }
            }
        }
        stepError.value = null
        if (step.value < 3) step.value++
    }
    fun prevStep() {
        if (step.value > 1)
            step.value-- }

    fun finish() {
        if (_selectedHabits.value.isEmpty()) {
            stepError.value = "Selecciona al menos un rasgo"; return
        }
        stepError.value = null
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading.value = true
            val user = User(
                id = userId,
                username = _username.value.trim(),
                email = authRepository.currentUser?.email ?: "",
                age = _age.value.toIntOrNull() ?: 0,
                location = _city.value.trim(),
                bio = _bio.value.trim(),
                profileImage = _profileImageUrl.value,
                habits = _selectedHabits.value,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            userRepository.saveUser(user).fold(
                onSuccess = { isDone.value = true },
                onFailure = { errorMessage.value = "Error al guardar el perfil" }
            )
            isLoading.value = false
        }
    }
}