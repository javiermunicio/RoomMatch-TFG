package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.data.repositories.AuthRepository
import com.example.roommatch_pmdm.data.repositories.RoomPostRepository
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.example.roommatch_pmdm.domain.usecase.DeleteRoomPostUseCase
import com.example.roommatch_pmdm.domain.usecase.ListRoomPostsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoomPostListViewModel(
    private val listRoomPostsUseCase:  ListRoomPostsUseCase,
    private val deleteRoomPostUseCase: DeleteRoomPostUseCase,
    private val authRepository:        AuthRepository           // ← añadido
) : ViewModel() {

    val roomPosts: StateFlow<List<RoomPost>> = listRoomPostsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UID del usuario autenticado (null si no hay sesión)
    val currentUserId: StateFlow<String?> = MutableStateFlow(authRepository.currentUser?.uid)

    fun delete(id: String) {
        viewModelScope.launch { deleteRoomPostUseCase(id) }
    }
}