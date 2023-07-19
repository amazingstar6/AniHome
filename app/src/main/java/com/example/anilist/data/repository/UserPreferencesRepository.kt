package com.example.anilist.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

enum class Title {
    ROMAJI,
    ENGLISH,
    NATIVE,
    USER_PREFERRED,
}

data class UserSettings(
    val accessCode: String,
    val expiresIn: String,
    val titleFormat: Title
)

class UserPreferencesRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {

    private val TAG: String = "UserSettingsRepo"

    private object PreferencesKeys {
        val ACCESS_CODE = stringPreferencesKey("ACCESS_CODE")
        val EXPIRES_IN = stringPreferencesKey("EXPIRES_IN")
        val TITLE_FORMAT = stringPreferencesKey("TITLE_FORMAT")
    }

    val userPreferencesFlow: Flow<UserSettings> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            mapUserSettings(preferences)
        }

    private fun mapUserSettings(preferences: Preferences): UserSettings {
        val accessCode = preferences[PreferencesKeys.ACCESS_CODE] ?: ""
        val expiresIn = preferences[PreferencesKeys.EXPIRES_IN] ?: ""
        val titleLanguage =
            Title.valueOf(preferences[PreferencesKeys.TITLE_FORMAT] ?: Title.USER_PREFERRED.name)
        return UserSettings(accessCode, expiresIn, titleLanguage)
    }

    suspend fun setAccessCode(accessCode: String) {
        // updateData handles data transactionally, ensuring that if the sort is updated at the same
        // time from another thread, we won't have conflicts
        dataStore.edit { preferences ->
            Log.i(TAG, "Access code in user preference repository parameter is $accessCode")
            preferences[PreferencesKeys.ACCESS_CODE] = accessCode
        }
    }


//    suspend fun saveToDataStore(userSettings: UserSettings) {
//        dataStore.edit { settings ->
//            settings[PreferencesKeys.ACCESS_CODE] = userSettings.accessCode
//            settings[PreferencesKeys.EXPIRES_IN] = userSettings.expiresIn
//        }
//    }

//    fun getFromDataStore() = dataStore.data.map {
//        UserSettings(
//            accessCode = it[PreferencesKeys.ACCESS_CODE] ?: "",
//            expiresIn = it[PreferencesKeys.EXPIRES_IN] ?: "",
//        )
//    }

    suspend fun clearDataStore() = dataStore.edit {
        it.clear()
    }

    suspend fun fetchInitialPreferences() =
        mapUserSettings(dataStore.data.first().toPreferences())

}