package com.example.roommatch_pmdm.domain.usecase

import com.example.roommatch_pmdm.data.repositories.ChatRepository

class SendMessageUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(
        currentUserId: String,
        otherUserId: String,
        content: String
    ): Result<Unit> = try {
        chatRepository.sendMessage(currentUserId, otherUserId, content)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}