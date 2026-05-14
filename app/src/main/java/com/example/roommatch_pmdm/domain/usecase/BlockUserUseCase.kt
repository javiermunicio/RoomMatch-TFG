package com.example.roommatch_pmdm.domain.usecase

import com.example.roommatch_pmdm.data.repositories.BlockRepository
import com.example.roommatch_pmdm.data.repositories.ChatRepository
import com.example.roommatch_pmdm.data.repositories.MatchRepository

class BlockUserUseCase(
    private val blockRepository: BlockRepository,
    private val chatRepository: ChatRepository,
    private val matchRepository: MatchRepository
) {
    suspend operator fun invoke(currentUserId: String, otherUserId: String) {
        blockRepository.blockUser(currentUserId, otherUserId)
        chatRepository.deleteConversation(currentUserId, otherUserId)
        matchRepository.deleteMatchAndLikes(currentUserId, otherUserId)
    }
}