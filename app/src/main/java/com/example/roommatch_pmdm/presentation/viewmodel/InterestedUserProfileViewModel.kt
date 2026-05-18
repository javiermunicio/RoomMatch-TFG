package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InterestedUserProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user      = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.getUser(userId).fold(
                onSuccess = { _user.value = it },
                onFailure = { }
            )
            _isLoading.value = false
        }
    }
}