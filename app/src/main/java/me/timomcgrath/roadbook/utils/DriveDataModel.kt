package me.timomcgrath.roadbook.utils

import android.location.Location

data class DriveDataModel (
    val dateTimestamp: String,
    val originLocation: Location,
    val destinationLocation: Location,
    val distanceTravelled: Int,
    val timeElapsed: Long,
    val timeOfDay: String,
    val weatherConditions: String
)
