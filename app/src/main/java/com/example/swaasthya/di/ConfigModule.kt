package com.example.swaasthya.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    @Provides
    @Singleton
    fun provideGoogleWebClientId(@ApplicationContext context: Context): String {
        // TODO: Move this to BuildConfig or environment variables
        // For now, this should be set via environment variable or BuildConfig
        return System.getenv("GOOGLE_WEB_CLIENT_ID") 
            ?: "659375898281-ff2sc78flq7jfao4qudhutn7f8is02fo.apps.googleusercontent.com"
    }

    @Provides
    @Singleton
    fun provideBaseUrl(@ApplicationContext context: Context): String {
        // TODO: Move this to BuildConfig or environment variables
        return System.getenv("API_BASE_URL") ?: "http://192.168.74.120:8080/"
    }
}
