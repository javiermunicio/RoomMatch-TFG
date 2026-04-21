package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.example.roommatch_pmdm.domain.usecase.AddRoomPostUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddRoomPostViewModel(
    private val addRoomPostUseCase: AddRoomPostUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _roomPost = MutableStateFlow(RoomPost())
    val roomPost: StateFlow<RoomPost> = _roomPost.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setTitle(v: String)       { _roomPost.value = _roomPost.value.copy(title = v) }
    fun setDescription(v: String) { _roomPost.value = _roomPost.value.copy(description = v) }
    fun setAddress(v: String)     { _roomPost.value = _roomPost.value.copy(address = v) }
    fun setCity(v: String)        { _roomPost.value = _roomPost.value.copy(city = v) }
    fun setPrice(v: Long)         { _roomPost.value = _roomPost.value.copy(price = v) }
    fun setRoommates(v: Int)      { _roomPost.value = _roomPost.value.copy(roommates = v) }
    fun setAvailableFrom(v: String) { _roomPost.value = _roomPost.value.copy(availableFrom = v) }

    fun reset() { _roomPost.value = RoomPost() }

    fun save() {
        val currentUser = authRepository.currentUser ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val post = _roomPost.value.copy(
                ownerId = currentUser.uid,
                ownerName = currentUser.email ?: "",
                createdAt = System.currentTimeMillis()
            )
            if (addRoomPostUseCase(post)) {
                _isSaved.value = true
            }
            _isLoading.value = false
        }
    }

    fun clearSaved() { _isSaved.value = false }
}