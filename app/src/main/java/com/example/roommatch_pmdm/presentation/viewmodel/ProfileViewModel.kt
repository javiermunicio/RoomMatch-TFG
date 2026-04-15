package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Cargar desde Firebase
                val mockUser = User(
                    id = "1",
                    username = "federica",
                    email = "federica@example.com",
                    fullName = "Federica",
                    age = 20,
                    location = "Centro, Madrid",
                    bio = "Soy una persona responsable y amable",
                    habits = listOf("Responsable", "Respetuosa", "Limpia"),
                    preferences = listOf("Empatica", "Tranquila", "Organizada")
                )
                _user.value = mockUser
                populateFields(mockUser)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun populateFields(user: User) {
        _username.value = user.username
        _age.value = user.age.toString()
        _location.value = user.location
        _bio.value = user.bio
        _budget.value = user.budget.toString()
        _selectedHabits.value = user.habits
        _selectedPreferences.value = user.preferences
    }

    fun toggleEditMode() {
        _isEditing.value = !_isEditing.value
    }

    fun onUsernameChanged(newUsername: String) {
        _username.value = newUsername
    }

    fun onAgeChanged(newAge: String) {
        _age.value = newAge
    }

    fun onLocationChanged(newLocation: String) {
        _location.value = newLocation
    }

    fun onBioChanged(newBio: String) {
        _bio.value = newBio
    }

    fun onBudgetChanged(newBudget: String) {
        _budget.value = newBudget
    }

    fun toggleHabit(habit: String) {
        _selectedHabits.value = if (habit in _selectedHabits.value) {
            _selectedHabits.value - habit
        } else {
            _selectedHabits.value + habit
        }
    }

    fun togglePreference(preference: String) {
        _selectedPreferences.value = if (preference in _selectedPreferences.value) {
            _selectedPreferences.value - preference
        } else {
            _selectedPreferences.value + preference
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedUser = _user.value?.copy(
                    username = _username.value,
                    age = _age.value.toIntOrNull() ?: 0,
                    location = _location.value,
                    bio = _bio.value,
                    budget = _budget.value.toIntOrNull() ?: 0,
                    habits = _selectedHabits.value,
                    preferences = _selectedPreferences.value,
                    updatedAt = System.currentTimeMillis()
                )

                if (updatedUser != null) {
                    // TODO: Guardar en Firebase
                    _user.value = updatedUser
                    _isEditing.value = false
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        val availableHabits = listOf(
            "Responsable", "Respetuosa", "Limpia", "Empatica", "Tranquila", "Organizada"
        )
        val availablePreferences = listOf(
            "Comunicativa", "Considerada", "Flexible", "Responsable", "Respetuosa", "Limpia"
        )
    }
}
