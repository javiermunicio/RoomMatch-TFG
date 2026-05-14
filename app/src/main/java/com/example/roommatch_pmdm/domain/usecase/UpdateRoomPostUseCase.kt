package com.example.roommatch_pmdm.domain.usecase

import com.example.roommatch_pmdm.data.repositories.RoomPostRepository
import com.example.roommatch_pmdm.domain.model.RoomPost

class UpdateRoomPostUseCase(private val repository: RoomPostRepository) {
    suspend operator fun invoke(roomPost: RoomPost): Boolean = repository.update(roomPost)
}