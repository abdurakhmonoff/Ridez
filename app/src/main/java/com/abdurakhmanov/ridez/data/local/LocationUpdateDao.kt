package com.abdurakhmanov.ridez.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationUpdateDao {

    @Query("SELECT * FROM location_updates ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLocationUpdate(): Flow<LocationUpdate?>

    @Insert
    suspend fun insert(locationUpdate: LocationUpdate)
}