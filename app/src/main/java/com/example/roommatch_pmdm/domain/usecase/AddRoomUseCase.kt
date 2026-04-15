package com.example.roommatch_pmdm.domain.usecase

import com.example.roommatch_pmdm.data.repositories.RoomRepository
import com.example.roommatch_pmdm.domain.model.Rooms


class AddRoomUseCase(val roomRepository: RoomRepository ) {
    suspend operator fun invoke (room: Rooms) : Boolean{
        return roomRepository.save(room)
    }
}