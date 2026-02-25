package com.example.roommatch_pmdm.domain.usecase

import com.example.roommatch_pmdm.data.repositories.RoomRepository
import com.example.roommatch_pmdm.domain.Rooms
import kotlinx.coroutines.flow.Flow

class ListRoomUseCase (val roomRepository: RoomRepository ) {
    operator fun invoke(): Flow<List<Rooms>> {
        return roomRepository.list()
    }
}
