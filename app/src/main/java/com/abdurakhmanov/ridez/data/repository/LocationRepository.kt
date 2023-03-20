package com.abdurakhmanov.ridez.data.repository

import com.abdurakhmanov.ridez.data.models.Location
import com.abdurakhmanov.ridez.data.source.local.LocationUpdate
import com.abdurakhmanov.ridez.data.source.local.LocationUpdateDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationUpdateDao: LocationUpdateDao
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLiveLocation(isRunning: StateFlow<Boolean>): Flow<Location?> {
        return isRunning.flatMapLatest { status ->
            if (status) {
                locationUpdateDao.getLatestLocationUpdate()
                    .mapLatest { it?.let { Location(it.latitude, it.longitude) } }
            } else {
                emptyFlow()
            }
        }
    }

    suspend fun insertLocationUpdate(locationUpdate: LocationUpdate) =
        locationUpdateDao.insert(locationUpdate)
}