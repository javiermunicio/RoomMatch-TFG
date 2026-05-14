package com.example.roommatch_pmdm.domain.usecase

import android.net.Uri
import com.example.roommatch_pmdm.data.remote.StorageRepository
import com.example.roommatch_pmdm.data.repositories.UserRepository
import com.example.roommatch_pmdm.domain.model.User

class UploadProfileImageUseCase(
    private val storageRepository: StorageRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uri: Uri, currentUser: User): Result<String> {
        val uploadResult = storageRepository.uploadProfileImage(uri)
        if (uploadResult.isFailure) return Result.failure(uploadResult.exceptionOrNull()!!)

        val url = uploadResult.getOrThrow()
        val updatedUser = currentUser.copy(profileImage = url, updatedAt = System.currentTimeMillis())
        userRepository.saveUser(updatedUser)
        return Result.success(url)
    }
}