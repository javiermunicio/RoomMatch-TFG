package com.example.roommatch_pmdm.domain.model

data class RoomPost(
    val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val title: String = "",
    val description: String = "",
    val address: String = "",
    val city: String = "",
    val price: Long = 0L,
    val roommates: Int = 0,
    val availableFrom: String = "",
    val images: List<String> = emptyList(),
    val createdAt: Long = 0L
)