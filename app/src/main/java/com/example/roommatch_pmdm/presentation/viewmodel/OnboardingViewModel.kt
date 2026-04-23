package com.example.roommatch_pmdm.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.StorageRepository
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

    val username        = MutableStateFlow("")
    val age             = MutableStateFlow("")
    val city            = MutableStateFlow("")
    val bio             = MutableStateFlow("")
    val selectedHabits  = MutableStateFlow<List<String>>(emptyList())
    val profileImageUrl = MutableStateFlow("")

    val isLoading        = MutableStateFlow(false)
    val isUploadingImage = MutableStateFlow(false)
    val isDone           = MutableStateFlow(false)
    val errorMessage     = MutableStateFlow<String?>(null)

    val availableHabits = listOf(
        "Madrugador", "Noctámbulo", "Ordenado", "Tranquilo",
        "Sociable", "Deportista", "Fumador", "No fumador",
        "Con mascotas", "Vegetariano", "Músico", "Gamer"
    )

    fun toggleHabit(habit: String) {
        selectedHabits.value = if (habit in selectedHabits.value)
            selectedHabits.value - habit
        else
            selectedHabits.value + habit
    }

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            isUploadingImage.value = true
            storageRepository.uploadProfileImage(uri).fold(
                onSuccess  = { profileImageUrl.value = it },
                onFailure  = { errorMessage.value = "Error al subir la imagen" }
            )
            isUploadingImage.value = false
        }
    }

    fun nextStep() { if (step.value < 3) step.value++ }
    fun prevStep() { if (step.value > 1) step.value-- }

    fun finish() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading.value = true
            val user = User(
                id           = userId,
                username     = username.value.trim(),
                email        = authRepository.currentUser?.email ?: "",
                age          = age.value.toIntOrNull() ?: 0,
                location     = city.value.trim(),
                bio          = bio.value.trim(),
                profileImage = profileImageUrl.value,
                habits       = selectedHabits.value,
                createdAt    = System.currentTimeMillis(),
                updatedAt    = System.currentTimeMillis()
            )
            userRepository.saveUser(user).fold(
                onSuccess = { isDone.value = true },
                onFailure = { errorMessage.value = "Error al guardar el perfil" }
            )
            isLoading.value = false
        }
    }
}