package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Campos editables
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _age = MutableStateFlow("")
    val age: StateFlow<String> = _age

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location

    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio

    private val _budget = MutableStateFlow("")
    val budget: StateFlow<String> = _budget

    private val _selectedHabits = MutableStateFlow<List<String>>(emptyList())
    val selectedHabits: StateFlow<List<String>> = _selectedHabits

    private val _selectedPreferences = MutableStateFlow<List<String>>(emptyList())
    val selectedPreferences: StateFlow<List<String>> = _selectedPreferences

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.getUser(userId).fold(
                onSuccess = { user ->
                    _user.value = user
                    populateFields(user)
                },
                onFailure = {
                    _errorMessage.value = "No se pudo cargar el perfil"
                }
            )
            _isLoading.value = false
        }
    }

    private fun populateFields(user: User) {
        _username.value = user.username
        _age.value = if (user.age > 0) user.age.toString() else ""
        _location.value = user.location
        _bio.value = user.bio
        _budget.value = if (user.budget > 0) user.budget.toString() else ""
        _selectedHabits.value = user.habits
        _selectedPreferences.value = user.preferences
    }

    fun toggleEditMode() { _isEditing.value = !_isEditing.value }
    fun onUsernameChanged(v: String) { _username.value = v }
    fun onAgeChanged(v: String) { _age.value = v }
    fun onLocationChanged(v: String) { _location.value = v }
    fun onBioChanged(v: String) { _bio.value = v }
    fun onBudgetChanged(v: String) { _budget.value = v }

    fun toggleHabit(habit: String) {
        _selectedHabits.value = if (habit in _selectedHabits.value)
            _selectedHabits.value - habit else _selectedHabits.value + habit
    }

    fun togglePreference(preference: String) {
        _selectedPreferences.value = if (preference in _selectedPreferences.value)
            _selectedPreferences.value - preference else _selectedPreferences.value + preference
    }

    fun saveProfile() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val updatedUser = (_user.value ?: User(id = userId)).copy(
                id = userId,
                username = _username.value,
                age = _age.value.toIntOrNull() ?: 0,
                location = _location.value,
                bio = _bio.value,
                budget = _budget.value.toIntOrNull() ?: 0,
                habits = _selectedHabits.value,
                preferences = _selectedPreferences.value,
                updatedAt = System.currentTimeMillis()
            )
            userRepository.saveUser(updatedUser).fold(
                onSuccess = {
                    _user.value = updatedUser
                    _isEditing.value = false
                    _isSaved.value = true
                },
                onFailure = {
                    _errorMessage.value = "Error al guardar el perfil"
                }
            )
            _isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun clearError() { _errorMessage.value = null }
    fun clearSaved() { _isSaved.value = false }

    companion object {
        val availableHabits = listOf(
            "Responsable", "Respetuosa", "Limpia", "Empática", "Tranquila", "Organizada"
        )
        val availablePreferences = listOf(
            "Comunicativa", "Considerada", "Flexible", "Responsable", "Respetuosa", "Limpia"
        )
    }
}