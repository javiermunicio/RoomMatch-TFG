package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.domain.model.RoomPost
import com.example.roommatch_pmdm.domain.usecase.DeleteRoomPostUseCase
import com.example.roommatch_pmdm.domain.usecase.ListRoomPostsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoomPostListViewModel(
    private val listRoomPostsUseCase: ListRoomPostsUseCase,
    private val deleteRoomPostUseCase: DeleteRoomPostUseCase
) : ViewModel() {

    val roomPosts: StateFlow<List<RoomPost>> = listRoomPostsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(id: String) {
        viewModelScope.launch { deleteRoomPostUseCase(id) }
    }
}