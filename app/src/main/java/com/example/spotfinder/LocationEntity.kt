package com.example.spotfinder

// Plain data holder for a row in the location table
data class LocationEntity(
    val id: Long = 0L,
    val address: String,
    val latitude: Double,
    val longitude: Double
)
