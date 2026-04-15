package com.example.roommatch_pmdm.domain.model

data class Match(
    val id: String = "",
    val userId1: String = "",
    val userId2: String = "",
    val matchedAt: Long = 0,
    val status: String = "active"
)

data class UserCard(
    val id: String = "",
    val username: String = "",
    val profileImage: String = "",
    val age: Int = 0,
    val location: String = "",
    val bio: String = "",
    val habits: List<String> = emptyList(),
    val preferences: List<String> = emptyList()
)
