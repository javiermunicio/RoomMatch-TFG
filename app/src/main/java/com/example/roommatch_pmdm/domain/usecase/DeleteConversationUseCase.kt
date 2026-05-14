package com.example.roommatch_pmdm.domain.usecase

import com.example.roommatch_pmdm.data.repositories.ChatRepository

class DeleteConversationUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(currentUserId: String, otherUserId: String) =
        chatRepository.deleteConversation(currentUserId, otherUserId)
}