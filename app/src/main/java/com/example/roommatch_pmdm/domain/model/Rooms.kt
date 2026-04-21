package com.example.roommatch_pmdm.domain.model
data class Rooms(
    val buildingTipe: String = "",
    val direction: String = "",
    val price: Long = 0L,
    val roomMate: String = "",
    val check: Boolean = false
)