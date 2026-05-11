package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.BlockRepository
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.example.roommatch_pmdm.domain.usecase.DeleteRoomPostUseCase
import com.example.roommatch_pmdm.domain.usecase.ListRoomPostsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoomPostListViewModel(
    private val listRoomPostsUseCase:  ListRoomPostsUseCase,
    private val deleteRoomPostUseCase: DeleteRoomPostUseCase,
    private val authRepository: AuthRepository,
    private val blockRepository: BlockRepository
) : ViewModel() {

    val filterCity      = MutableStateFlow("")  // ciudad (parcial, ignora mayúsc.)
    val filterMaxPrice  = MutableStateFlow("")  // presupuesto máximo en €
    val filterRoommates = MutableStateFlow("")  // número exacto de compañeros

    private val _blockedIds = MutableStateFlow<Set<String>>(emptySet())


    private val _allPosts = listRoomPostsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val uid = authRepository.currentUser?.uid ?: return@launch
            val blockedByMe   = blockRepository.getBlockedUserIds(uid).toSet()
            val blockedByThem = blockRepository.getUsersWhoBlockedMe(uid).toSet()
            _blockedIds.value = blockedByMe + blockedByThem
        }
    }

    val roomPosts: StateFlow<List<RoomPost>> = combine(
        _allPosts, filterCity, filterMaxPrice, filterRoommates, _blockedIds
    ) { posts, city, maxPrice, roommates, blocked ->
        posts.filter { post ->
            val cityOk      = city.isBlank() || post.city.contains(city.trim(), ignoreCase = true)
            val priceOk     = maxPrice.isBlank() || (maxPrice.toLongOrNull()?.let { post.price <= it } ?: true)
            val roommatesOk = roommates.isBlank() || (roommates.toIntOrNull()?.let { post.roommates == it } ?: true)
            val notBlocked  = post.ownerId !in blocked
            cityOk && priceOk && roommatesOk && notBlocked
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasActiveFilters: StateFlow<Boolean> = combine(
        filterCity, filterMaxPrice, filterRoommates
    ) { city, price, roommates ->
        city.isNotBlank() || price.isNotBlank() || roommates.isNotBlank()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun clearFilters() {
        filterCity.value      = ""
        filterMaxPrice.value  = ""
        filterRoommates.value = ""
    }

    private val _currentUserId = MutableStateFlow(authRepository.currentUser?.uid)
    val currentUserId: StateFlow<String?> = _currentUserId

    fun delete(id: String) {
        viewModelScope.launch { deleteRoomPostUseCase(id) }
    }
}