package com.abdurakhmanov.ridez.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_updates")
data class LocationUpdate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)