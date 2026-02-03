package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.roommatch_pmdm.domain.model.Rooms
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainScreenViewModel : ViewModel(){
    private val _rooms = MutableStateFlow<List<Rooms>>(
        listOf(
            Rooms("Piso", "Dirección Aleatoria, 1", 400,"Compañero 1"),
            Rooms("Piso", "Dirección Aleatoria, 2", 700,"Compañero 2"),
            Rooms("Chalet", "Dirección Aleatoria, 3", 1200,"ComCompañero pañero 3"),
            Rooms("Piso", "Dirección Aleatoria, 4", 450,"Compañero 4"),
            Rooms("Chalet", "Dirección Aleatoria, 5", 800,"Compañero 5"),
            Rooms("Chalet", "Dirección Aleatoria, 6", 500,"Compañero 6")

        )
    )
    val rooms = _rooms.asStateFlow()

    fun toggleCheck(direction: String) {
        _rooms.value = _rooms.value.map { room ->
            if (room.direction == direction) {
                room.copy(check = !room.check)
            } else {
                room
            }
        }
    }

}