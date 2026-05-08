package com.example.roommatch_pmdm.presentation.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.MatchRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.model.User
import com.example.roommatch_pmdm.domain.model.UserCard
import com.example.roommatch_pmdm.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class MatchingViewModel(
    private val matchRepository: MatchRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
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
                val currentUser = userRepository.getUser(currentUserId).getOrNull()

                val users = matchRepository.getUsersToSwipe(currentUserId)
                val cards = users.map { user ->
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

                _userCards.value = if (currentUser == null) {
                    cards
                } else {
                    cards
                        .map { card ->
                            val score = computeCompatibilityScore(
                                currentUser = currentUser,
                                candidate   = users.first { it.id == card.id }
                            )
                            card to score
                        }
                        .sortedByDescending { (_, score) -> score }
                        .map { (card, _) -> card }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Puntuación de compatibilidad entre el usuario actual y un candidato.
     *
     * | Criterio                          | Puntos |
     * |-----------------------------------|--------|
     * | Misma ciudad                      |   10   |
     * | Cada hábito en común              |    3   |
     * | Presupuesto dentro de ±200 €      |    5   |
     * | Presupuesto dentro de ±500 €      |    2   |
     * | Perfil completo (bio + foto)      |    2   |
     */
    private fun computeCompatibilityScore(currentUser: User, candidate: User): Int {
        var score = 0

        // ── Ciudad ────────────────────────────────────────────────────────────
        if (currentUser.location.isNotBlank() &&
            candidate.location.equals(currentUser.location, ignoreCase = true)
        ) {
            score += 10
        }

        // ── Hábitos en común ─────────────────────────────────────────────────
        val commonHabits = candidate.habits.count { it in currentUser.habits }
        score += commonHabits * 3

        // ── Presupuesto similar ───────────────────────────────────────────────
        if (currentUser.budget > 0 && candidate.budget > 0) {
            val budgetDiff = abs(currentUser.budget - candidate.budget)
            score += when {
                budgetDiff <= 200 -> 5
                budgetDiff <= 500 -> 2
                else              -> 0
            }
        }

        // ── Perfil completo (bio + foto) ──────────────────────────────────────
        if (candidate.bio.isNotBlank() && candidate.profileImage.isNotBlank()) {
            score += 2
        }

        return score
    }

    fun onLike() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        val currentCard   = _userCards.value.getOrNull(_currentIndex.value) ?: return

        viewModelScope.launch {
            val isMatch = matchRepository.saveLikeAndCheckMatch(currentUserId, currentCard.id)
            if (isMatch) {
                _matchedUser.value    = currentCard
                _showMatchPopup.value = true

                val canNotify = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else true

                if (canNotify) {
                    NotificationHelper.showMatchNotification(
                        context         = context,
                        matchedUsername = currentCard.username
                    )
                }
            }
            moveToNextCard()
        }
    }

    fun onPass() { moveToNextCard() }

    private fun moveToNextCard() { _currentIndex.value += 1 }

    fun dismissMatchPopup() { _showMatchPopup.value = false }
}