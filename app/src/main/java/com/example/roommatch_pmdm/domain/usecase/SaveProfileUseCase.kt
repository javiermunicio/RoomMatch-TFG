package com.example.roommatch_pmdm.domain.usecase

import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.model.User

class SaveProfileUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(user: User): Result<Unit> = userRepository.saveUser(user)
}