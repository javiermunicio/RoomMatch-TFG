package com.example.roommatch_pmdm.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.model.UserCard
import com.example.roommatch_pmdm.domain.usecase.GetUsersToSwipeUseCase
import com.example.roommatch_pmdm.domain.usecase.SaveLikeAndCheckMatchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatchingViewModel(
    private val authRepository: AuthRepository,
    private val getUsersToSwipeUseCase: GetUsersToSwipeUseCase,
    private val saveLikeAndCheckMatchUseCase: SaveLikeAndCheckMatchUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private var currentUsername = ""
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
        loadCurrentUsername()
    }
    private fun loadCurrentUsername() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            currentUsername = userRepository.getUser(uid).getOrNull()?.username ?: ""
        }
    }
    private fun loadUserCards() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _userCards.value = getUsersToSwipeUseCase(currentUserId).map { user ->
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
            val isMatch = saveLikeAndCheckMatchUseCase(
                currentUserId    = currentUserId,
                currentUsername  = currentUsername,
                targetUserId     = currentCard.id,
                targetUsername   = currentCard.username
            )
            if (isMatch) {
                _matchedUser.value    = currentCard
                _showMatchPopup.value = true
            }
            moveToNextCard()
        }
    }

    fun onPass() { moveToNextCard() }

    private fun moveToNextCard() { _currentIndex.value += 1 }

    fun dismissMatchPopup() { _showMatchPopup.value = false }
    fun reload() {
        _currentIndex.value = 0
        loadUserCards()
    }
}
