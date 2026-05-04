package com.example.roommatch_pmdm.domain.model

data class Interest(
    val id: String = "",
    val postId: String = "",
    val postOwnerId: String = "",
    val interestedUserId: String = "",
    val interestedUsername: String = "",
    val createdAt: Long = 0L
)