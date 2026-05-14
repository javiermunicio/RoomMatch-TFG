package com.example.roommatch_pmdm.domain.usecase

import com.example.roommatch_pmdm.data.repositories.RoomPostRepository
import com.example.roommatch_pmdm.domain.model.RoomPost

class GetRoomPostByIdUseCase(private val repository: RoomPostRepository) {
    suspend operator fun invoke(id: String): Result<RoomPost> = repository.getById(id)
}