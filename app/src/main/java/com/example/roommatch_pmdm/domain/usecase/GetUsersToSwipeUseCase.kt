package com.example.roommatch_pmdm.domain.usecase

import com.example.roommatch_pmdm.data.repositories.BlockRepository
import com.example.roommatch_pmdm.data.repositories.MatchRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.model.User
import kotlin.math.abs

class GetUsersToSwipeUseCase(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository,
    private val blockRepository: BlockRepository
) {
    suspend operator fun invoke(currentUserId: String): List<User> {
        val currentUser   = userRepository.getUser(currentUserId).getOrNull()
        val blockedByMe   = blockRepository.getBlockedUserIds(currentUserId).toSet()
        val blockedByThem = blockRepository.getUsersWhoBlockedMe(currentUserId).toSet()
        val allBlocked    = blockedByMe + blockedByThem

        val candidates = matchRepository.getUsersToSwipe(currentUserId)
            .filter { it.id !in allBlocked }

        return if (currentUser == null) {
            candidates
        } else {
            candidates.sortedByDescending { computeCompatibilityScore(currentUser, it) }
        }
    }

    private fun computeCompatibilityScore(currentUser: User, candidate: User): Int {
        var score = 0

        if (currentUser.location.isNotBlank() &&
            candidate.location.equals(currentUser.location, ignoreCase = true)
        ) score += 10

        score += candidate.habits.count { it in currentUser.habits } * 3

        if (currentUser.budget > 0 && candidate.budget > 0) {
            val diff = abs(currentUser.budget - candidate.budget)
            score += when {
                diff <= 200 -> 5
                diff <= 500 -> 2
                else        -> 0
            }
        }

        if (candidate.bio.isNotBlank() && candidate.profileImage.isNotBlank()) score += 2

        return score
    }
}