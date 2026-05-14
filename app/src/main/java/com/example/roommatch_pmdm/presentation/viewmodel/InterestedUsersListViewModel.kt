package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.InterestRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.model.InterestedUserItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InterestedUsersListViewModel(
    private val interestRepository: InterestRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<InterestedUserItem>>(emptyList())
    val items: StateFlow<List<InterestedUserItem>> = _items

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadInterests(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            interestRepository.getInterestedUsersFlow(postId).collect { interests ->
                _items.value = interests.map { interest ->
                    val user = userRepository.getUser(interest.interestedUserId).getOrNull()
                    InterestedUserItem(interest, user)
                }
                _isLoading.value = false
            }
        }
    }
}