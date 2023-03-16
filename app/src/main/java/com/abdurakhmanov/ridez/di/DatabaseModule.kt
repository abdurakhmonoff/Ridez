package com.abdurakhmanov.ridez.di

import android.content.Context
import com.abdurakhmanov.ridez.data.source.local.AppDatabase
import com.abdurakhmanov.ridez.data.source.local.LocationUpdateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideLocationUpdateDao(appDatabase: AppDatabase): LocationUpdateDao {
        return appDatabase.locationUpdateDao()
    }
}