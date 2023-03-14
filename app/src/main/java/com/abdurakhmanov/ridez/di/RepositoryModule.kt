package com.abdurakhmanov.ridez.di

import com.abdurakhmanov.ridez.data.local.LocationUpdateDao
import com.abdurakhmanov.ridez.data.repository.LocationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideLocationRepository(locationUpdateDao: LocationUpdateDao): LocationRepository {
        return LocationRepository(locationUpdateDao)
    }
}