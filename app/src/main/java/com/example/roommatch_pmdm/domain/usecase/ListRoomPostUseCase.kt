// ListRoomPostsUseCase.kt
package com.example.roommatch_pmdm.domain.usecase

import com.example.roommatch_pmdm.data.repositories.RoomPostRepository
import com.example.roommatch_pmdm.domain.model.RoomPost
import kotlinx.coroutines.flow.Flow

class ListRoomPostsUseCase(private val repository: RoomPostRepository) {
    operator fun invoke(): Flow<List<RoomPost>> = repository.listAll()
}