package com.abdurakhmanov.ridez.di

import com.abdurakhmanov.ridez.data.repository.LocationRepository
import com.abdurakhmanov.ridez.data.source.local.LocationUpdateDao
import com.abdurakhmanov.ridez.data.source.remote.GeocoderApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideLocationRepository(
        locationUpdateDao: LocationUpdateDao,
        geocoderApiService: GeocoderApiService
    ): LocationRepository {
        return LocationRepository(locationUpdateDao, geocoderApiService)
    }
}