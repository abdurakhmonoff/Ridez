package com.abdurakhmanov.ridez.di

import com.abdurakhmanov.ridez.data.repository.LocationRepository
import com.abdurakhmanov.ridez.data.source.local.LocationUpdateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideLocationRepository(
        locationUpdateDao: LocationUpdateDao
    ): LocationRepository {
        return LocationRepository(locationUpdateDao)
    }
}