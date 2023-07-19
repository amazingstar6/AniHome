package com.example.anilist

import android.app.Application
import com.example.anilist.data.repository.MediaRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AniHomeApplication: Application() {
//    @Module
//    @InstallIn(MainActivity::class)
//    object AppModule {
//
//        @Provides
//        fun provideUserSettings(dataStore: DataStore<Preferences>): DataStoreManager {
//            return DataStoreManager(dataStore)
//        }
//    }
}