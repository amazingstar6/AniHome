package com.example.anilist.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

enum class TitleFormat {
    ROMAJI,
    ENGLISH,
    NATIVE,
    USER_PREFERRED,
}

enum class Theme {
    SYSTEM_DEFAULT,
    LIGHT,
    DARK;

    override fun toString(): String {
        return when (this) {
            SYSTEM_DEFAULT -> "System default"
            LIGHT -> "Light"
            DARK -> "Dark"
        }
    }
}

data class UserSettings(
    val accessCode: String,
    val tokenType: String,
    val expiresIn: String,
    val titleFormat: TitleFormat,
    val theme: Theme
)

private const val TAG: String = "UserSettingsRepo"

class UserDataRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
//    companion object {
//        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "USER_SETTINGS")
//        private val DATA_STORE_KEY = "USER_SETTINGS"
//    }

    private object PreferencesKeys {
        val ACCESS_CODE = stringPreferencesKey("ACCESS_CODE")
        val TOKEN_TYPE = stringPreferencesKey("TOKEN_TYPE")
        val EXPIRES_IN = stringPreferencesKey("EXPIRES_IN")
        val TITLE_FORMAT = stringPreferencesKey("TITLE_FORMAT")
        val THEME = stringPreferencesKey("THEME")
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
            val accessCode = preferences[PreferencesKeys.ACCESS_CODE] ?: ""
            val expiresIn = preferences[PreferencesKeys.EXPIRES_IN] ?: ""
            val tokenType = preferences[PreferencesKeys.TOKEN_TYPE] ?: ""
            val titleFormat =
                TitleFormat.valueOf(
                    preferences[PreferencesKeys.TITLE_FORMAT] ?: TitleFormat.USER_PREFERRED.name
                )
            val theme =
                Theme.valueOf(preferences[PreferencesKeys.THEME] ?: Theme.SYSTEM_DEFAULT.name)
            UserSettings(
                accessCode = accessCode,
                tokenType = tokenType,
                expiresIn = expiresIn,
                titleFormat = titleFormat,
                theme = theme
            )
        }

    suspend fun saveTheme(theme: Theme) {
        dataStore.edit { settings ->
            Log.d(TAG, "Saving theme $theme in data store")
            settings[PreferencesKeys.THEME] = theme.name
        }
    }

    suspend fun saveAccessCode(accessCode: String, tokenType: String, expiresIn: String) {
        // updateData handles data transactionally, ensuring that if the sort is updated at the same
        // time from another thread, we won't have conflicts
        dataStore.edit { preferences ->
            Log.i(TAG, "Access code in user preference repository parameter is $accessCode")
            preferences[PreferencesKeys.ACCESS_CODE] = accessCode
            preferences[PreferencesKeys.TOKEN_TYPE] = tokenType
            preferences[PreferencesKeys.EXPIRES_IN] = expiresIn
        }
    }

    suspend fun saveTitle(titleFormat: TitleFormat) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TITLE_FORMAT] = titleFormat.name
        }
    }

    suspend fun logOut() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCESS_CODE] = ""
            preferences[PreferencesKeys.TOKEN_TYPE] = ""
            preferences[PreferencesKeys.EXPIRES_IN] = ""
        }
    }
}
