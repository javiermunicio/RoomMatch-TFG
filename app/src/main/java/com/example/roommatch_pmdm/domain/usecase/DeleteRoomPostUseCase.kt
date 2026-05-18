package com.example.roommatch_pmdm.domain.usecase

import com.example.roommatch_pmdm.data.repositories.RoomPostRepository

class DeleteRoomPostUseCase(private val repository: RoomPostRepository) {
    suspend operator fun invoke(id: String): Boolean = repository.delete(id)
}