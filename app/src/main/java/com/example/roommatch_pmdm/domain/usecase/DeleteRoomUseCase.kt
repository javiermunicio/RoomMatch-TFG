package com.example.roommatch_pmdm.domain.usecase

import com.example.roommatch_pmdm.data.repositories.RoomRepository

class DeleteRoomUseCase (val roomRepository: RoomRepository){
    suspend operator fun invoke (id: String) : Boolean{
        return roomRepository.delete(id)
    }
}