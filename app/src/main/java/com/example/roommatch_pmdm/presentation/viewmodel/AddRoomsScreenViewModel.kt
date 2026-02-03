package com.example.roommatch_pmdm.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.roommatch_pmdm.domain.model.Rooms
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AddRoomsScreenViewModel : ViewModel(){
    private val _room = MutableStateFlow(Rooms("", "", 400, ""))
    val room : StateFlow<Rooms> = _room.asStateFlow()

    fun setBuildingTipe (buildingTipe : String){
        _room.value = _room.value.copy(buildingTipe = buildingTipe)
    }
    fun setDirection (direction : String){
        _room.value = _room.value.copy(direction = direction)
    }
    fun setPrice (price : Long){
        _room.value = _room.value.copy(price = price)
    }
    fun setRoomMate(roomMate: String){
        _room.value = _room.value.copy(roomMate = roomMate)
    }

    fun reset() {
        _room.value = Rooms("", "", 400,"")
    }
}