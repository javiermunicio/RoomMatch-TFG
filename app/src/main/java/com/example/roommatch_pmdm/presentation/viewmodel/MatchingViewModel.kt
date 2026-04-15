package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.domain.model.UserCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatchingViewModel : ViewModel() {

    private val _userCards = MutableStateFlow<List<UserCard>>(emptyList())
    val userCards: StateFlow<List<UserCard>> = _userCards

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showMatchPopup = MutableStateFlow(false)
    val showMatchPopup: StateFlow<Boolean> = _showMatchPopup

    private val _matchedUser = MutableStateFlow<UserCard?>(null)
    val matchedUser: StateFlow<UserCard?> = _matchedUser

    init {
        loadUserCards()
    }

    private fun loadUserCards() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Cargar desde Firebase
                _userCards.value = generateMockUsers()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onLike() {
        viewModelScope.launch {
            val currentCard = _userCards.value.getOrNull(_currentIndex.value)
            if (currentCard != null) {
                checkMatch(currentCard)
            }
            moveToNextCard()
        }
    }

    fun onPass() {
        moveToNextCard()
    }

    private fun moveToNextCard() {
        if (_currentIndex.value < _userCards.value.size - 1) {
            _currentIndex.value += 1
        }
    }

    private fun checkMatch(card: UserCard) {
        // TODO: Verificar si es un match mutuo en Firebase
        // Simular match aleatorio
        if (Math.random() > 0.5) {
            _matchedUser.value = card
            _showMatchPopup.value = true
        }
    }

    fun dismissMatchPopup() {
        _showMatchPopup.value = false
    }

    private fun generateMockUsers(): List<UserCard> {
        return listOf(
            UserCard(
                id = "1",
                username = "usuario1",
                profileImage = "",
                age = 25,
                location = "Madrid",
                bio = "Me encanta viajar y conocer gente nueva",
                habits = listOf("Responsable", "Limpia"),
                preferences = listOf("Tranquilidad", "Luz natural")
            ),
            UserCard(
                id = "2",
                username = "usuario2",
                profileImage = "",
                age = 24,
                location = "Barcelona",
                bio = "Estudiante de ingeniería",
                habits = listOf("Empática", "Organizada"),
                preferences = listOf("Zona céntrica", "Bien comunicada")
            )
        )
    }
}
