package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.InterestRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.model.Interest
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RoomPostDetailViewModel(
    private val interestRepository: InterestRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore
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
            try {
                // Cargar el post desde Firestore
                val doc = firestore.collection("roomPosts").document(postId).get().await()
                val roomPost = doc.toObject(RoomPost::class.java)?.copy(id = doc.id)
                _post.value = roomPost

                if (roomPost != null) {
                    // Comprobar si el usuario actual es el dueño
                    _isOwner.value = roomPost.ownerId == currentUserId

                    // Comprobar si ya mostró interés
                    _isInterested.value = interestRepository.hasInterest(currentUserId, postId)

                    // Escuchar el contador de interesados en tiempo real
                    launch {
                        interestRepository.getInterestCountFlow(postId).collect { count ->
                            _interestCount.value = count
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "No se pudo cargar el anuncio"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleInterest() {
        val currentUserId = authRepository.currentUser?.uid ?: return
        val postId = _post.value?.id ?: return
        val postOwnerId = _post.value?.ownerId ?: return

        viewModelScope.launch {
            _isLoading.value = true
            if (_isInterested.value) {
                // Retirar interés
                interestRepository.removeInterest(currentUserId, postId).fold(
                    onSuccess = {
                        _isInterested.value = false
                        _successMessage.value = "Has retirado tu interés"
                    },
                    onFailure = { _errorMessage.value = "Error al retirar el interés" }
                )
            } else {
                // Mostrar interés — obtenemos el username del usuario actual
                val userResult = userRepository.getUser(currentUserId)
                val username = userResult.getOrNull()?.username ?: "Usuario"

                val interest = Interest(
                    postId = postId,
                    postOwnerId = postOwnerId,
                    interestedUserId = currentUserId,
                    interestedUsername = username,
                    createdAt = System.currentTimeMillis()
                )
                interestRepository.addInterest(interest).fold(
                    onSuccess = {
                        _isInterested.value = true
                        _successMessage.value = "¡El dueño del piso ha sido notificado de tu interés!"
                    },
                    onFailure = { _errorMessage.value = "Error al registrar el interés" }
                )
            }
            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}