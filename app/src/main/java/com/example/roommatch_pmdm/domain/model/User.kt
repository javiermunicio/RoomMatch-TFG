package com.example.roommatch_pmdm.domain.model

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val age: Int = 0,
    val location: String = "",
    val bio: String = "",
    val profileImage: String = "",
    val habits: List<String> = emptyList(),
    val preferences: List<String> = emptyList(),
    val budget: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val fcmToken: String = ""
)

data class UserProfile(
    val id: String = "",
    val username: String = "",
    val age: Int = 0,
    val location: String = "",
    val profileImage: String = "",
    val habits: List<String> = emptyList(),
    val preferences: List<String> = emptyList()
)
