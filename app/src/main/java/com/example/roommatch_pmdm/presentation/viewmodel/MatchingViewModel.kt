package com.example.roommatch_pmdm.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.MatchRepository
import com.example.roommatch_pmdm.domain.model.UserCard
import com.example.roommatch_pmdm.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatchingViewModel(
    private val matchRepository: MatchRepository,
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

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

    init { loadUserCards() }

    private fun loadUserCards() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val users = matchRepository.getUsersToSwipe(currentUserId)
                _userCards.value = users.map { user ->
                    UserCard(
                        id           = user.id,
                        username     = user.username,
                        profileImage = user.profileImage,
                        age          = user.age,
                        location     = user.location,
                        bio          = user.bio,
                        habits       = user.habits,
                        preferences  = user.preferences
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onLike() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        val currentCard   = _userCards.value.getOrNull(_currentIndex.value) ?: return

        viewModelScope.launch {
            val isMatch = matchRepository.saveLikeAndCheckMatch(currentUserId, currentCard.id)
            if (isMatch) {
                _matchedUser.value    = currentCard
                _showMatchPopup.value = true

                NotificationHelper.showMatchNotification(
                    context  = context,
                    matchedUsername = currentCard.username
                )
            }
            moveToNextCard()
        }
    }

    fun onPass() { moveToNextCard() }

    private fun moveToNextCard() { _currentIndex.value += 1 }

    fun dismissMatchPopup() { _showMatchPopup.value = false }
}