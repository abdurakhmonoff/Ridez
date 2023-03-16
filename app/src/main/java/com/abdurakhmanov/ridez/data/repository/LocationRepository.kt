package com.abdurakhmanov.ridez.data.repository

import com.abdurakhmanov.ridez.data.models.Location
import com.abdurakhmanov.ridez.data.response.CurrentAddress
import com.abdurakhmanov.ridez.data.source.local.LocationUpdate
import com.abdurakhmanov.ridez.data.source.local.LocationUpdateDao
import com.abdurakhmanov.ridez.data.source.remote.GeocoderApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationUpdateDao: LocationUpdateDao,
    private val geocoderApiService: GeocoderApiService
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

    suspend fun getAddress(apiKey: String, lat: Double, lon: Double?): Response<CurrentAddress?> {
        return geocoderApiService.getAddress(apiKey, lat, lon)
    }
}