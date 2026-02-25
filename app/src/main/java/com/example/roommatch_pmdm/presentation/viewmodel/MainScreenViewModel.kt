package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roommatch_pmdm.domain.model.Rooms
import com.example.roommatch_pmdm.domain.usecase.DeleteRoomUseCase
import com.example.roommatch_pmdm.domain.usecase.ListRoomUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.map

class MainScreenViewModel(
    private val listRoomUseCase: ListRoomUseCase,
    private val deleteRoomUseCase: DeleteRoomUseCase
): ViewModel(){
    private val _rooms = listRoomUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val rooms:StateFlow<List<Rooms>> = _rooms

    fun deleteMovie(id: String) {
        viewModelScope.launch {
            deleteRoomUseCase(id) }
    }
}
