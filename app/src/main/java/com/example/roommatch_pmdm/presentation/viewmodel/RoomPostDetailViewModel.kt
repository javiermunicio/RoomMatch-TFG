package com.example.roommatch_pmdm.presentation.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.InterestRepository
import com.example.roommatch_pmdm.data.repositories.RoomPostRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.model.Interest
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.example.roommatch_pmdm.domain.usecase.GetRoomPostByIdUseCase
import com.example.roommatch_pmdm.domain.usecase.ToggleInterestUseCase
import com.example.roommatch_pmdm.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomPostDetailViewModel(
    private val toggleInterestUseCase: ToggleInterestUseCase,
    private val authRepository: AuthRepository,
    private val getRoomPostByIdUseCase: GetRoomPostByIdUseCase,
    private val interestRepository: InterestRepository
) : ViewModel() {

    private val _post = MutableStateFlow<RoomPost?>(null)
    val post: StateFlow<RoomPost?> = _post

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _interestCount = MutableStateFlow(0)
    val interestCount: StateFlow<Int> = _interestCount

    private val _isInterested = MutableStateFlow(false)
    val isInterested: StateFlow<Boolean> = _isInterested

    private val _isOwner = MutableStateFlow(false)
    val isOwner: StateFlow<Boolean> = _isOwner

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun loadPost(postId: String) {
        val currentUserId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            getRoomPostByIdUseCase(postId).fold(
                onSuccess = { roomPost ->
                    _post.value = roomPost
                    _isOwner.value = roomPost.ownerId == currentUserId
                    _isInterested.value = interestRepository.hasInterest(currentUserId, postId)
                    launch {
                        interestRepository.getInterestCountFlow(postId).collect { count ->
                            _interestCount.value = count
                        }
                    }
                },
                onFailure = { _errorMessage.value = "No se pudo cargar el anuncio" }
            )
            _isLoading.value = false
        }
    }

    fun toggleInterest() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        val post = _post.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            toggleInterestUseCase(currentUserId, post, _isInterested.value).fold(
                onSuccess = { newState ->
                    _isInterested.value = newState
                    _successMessage.value = if (newState)
                        "¡El dueño del piso ha sido notificado de tu interés!"
                    else
                        "Has retirado tu interés"
                },
                onFailure = { _errorMessage.value = "Error al actualizar el interés" }
            )
            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value   = null
        _successMessage.value = null
    }
}