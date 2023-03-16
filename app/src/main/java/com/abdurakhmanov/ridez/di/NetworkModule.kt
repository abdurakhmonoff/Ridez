package com.abdurakhmanov.ridez.di

import com.abdurakhmanov.ridez.data.source.remote.GeocoderApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideRetrofit(): Retrofit {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory()).build()
        return Retrofit.Builder()
            .baseUrl("https://geocod.xyz/api/public/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    fun provideGeocodApiService(retrofit: Retrofit): GeocoderApiService {
        return retrofit.create(GeocoderApiService::class.java)
    }
}