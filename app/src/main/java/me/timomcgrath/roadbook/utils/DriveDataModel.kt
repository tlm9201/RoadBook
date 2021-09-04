package me.timomcgrath.roadbook.utils

data class DriveDataModel (
    val dateTimestamp: String,
    val originLocationLat: Double,
    val originLocationLon: Double,
    val destinationLocationLat: Double,
    val destinationLocationLon: Double,
    val distanceTravelled: Double,
    val timeElapsed: Long,
    val timeOfDay: String,
    val weatherConditions: String,
    val units: String
)
