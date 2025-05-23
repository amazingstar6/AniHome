package com.kevin.anihome.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val USER_PREFERENCES = "USER_PREFERENCES"

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {
    @Singleton
    @Provides
    fun providePreferencesDataStore(
        @ApplicationContext appContext: Context,
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler =
                ReplaceFileCorruptionHandler(
                    produceNewData = { emptyPreferences() },
                ),
            produceFile = { appContext.preferencesDataStoreFile(USER_PREFERENCES) },
        )
    }
}
