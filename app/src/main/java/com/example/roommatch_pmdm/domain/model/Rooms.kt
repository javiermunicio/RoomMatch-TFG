package com.example.roommatch_pmdm.domain.model


data class Rooms(
    val buildingTipe : String,
    val direction: String,
    val price: Long,
    val roomMate: String,
    val check : Boolean = false
)