package com.kevin.anihome.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kevin.anihome.data.models.AniMediaListSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class TitleFormat {
    ROMAJI,
    ENGLISH,
    NATIVE,
    USER_PREFERRED,
}

enum class Theme {
    SYSTEM_DEFAULT,
    LIGHT,
    DARK,
    ;

    override fun toString(): String {
        return when (this) {
            SYSTEM_DEFAULT -> "System default"
            LIGHT -> "Light"
            DARK -> "Dark"
        }
    }
}

data class UserSettings(
    val userId: Int,
    val accessCode: String,
    val tokenType: String,
    val expiresIn: String,
    val titleFormat: TitleFormat,
    val theme: Theme,
    val mediaListSort: AniMediaListSort,
)

@Singleton
class UserDataRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
//    companion object {
//        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "USER_SETTINGS")
//        private val DATA_STORE_KEY = "USER_SETTINGS"
//    }

        private object PreferencesKeys {
            val USER_ID = intPreferencesKey("USER_ID")
            val ACCESS_CODE = stringPreferencesKey("ACCESS_CODE")
            val TOKEN_TYPE = stringPreferencesKey("TOKEN_TYPE")
            val EXPIRES_IN = stringPreferencesKey("EXPIRES_IN")
            val TITLE_FORMAT = stringPreferencesKey("TITLE_FORMAT")
            val THEME = stringPreferencesKey("THEME")
            val MEDIA_LIST_SORT = stringPreferencesKey("MEDIA_LIST_SORT")
        }

        val userPreferencesFlow: Flow<UserSettings> =
            dataStore.data
                .catch { exception ->
                    // dataStore.data throws an IOException when an error is encountered when reading data
                    if (exception is IOException) {
                        Timber.e(exception, "Error reading preferences.")
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
                            preferences[PreferencesKeys.TITLE_FORMAT] ?: TitleFormat.ROMAJI.name,
                        )
                    val theme =
                        Theme.valueOf(preferences[PreferencesKeys.THEME] ?: Theme.SYSTEM_DEFAULT.name)
                    val userId = preferences[PreferencesKeys.USER_ID] ?: -1
                    val mediaListSort = preferences[PreferencesKeys.MEDIA_LIST_SORT] ?: AniMediaListSort.UPDATED_TIME_DESC.name
                    UserSettings(
                        accessCode = accessCode,
                        tokenType = tokenType,
                        expiresIn = expiresIn,
                        titleFormat = titleFormat,
                        theme = theme,
                        userId = userId,
                        mediaListSort = AniMediaListSort.valueOf(mediaListSort),
                    )
                }

        suspend fun saveTheme(theme: Theme) {
            dataStore.edit { settings ->
                settings[PreferencesKeys.THEME] = theme.name
            }
        }

        suspend fun saveAccessCode(
            accessCode: String,
            tokenType: String,
            expiresIn: String,
        ) {
            // updateData handles data transactional, ensuring that if the sort is updated at the same
            // time from another thread, we won't have conflicts
            dataStore.edit { preferences ->
                Timber.i("Access code in user preference repository parameter is $accessCode")
                preferences[PreferencesKeys.ACCESS_CODE] = accessCode
                preferences[PreferencesKeys.TOKEN_TYPE] = tokenType
                preferences[PreferencesKeys.EXPIRES_IN] = expiresIn
            }
            Timber.d("Done saving access code")
        }

        suspend fun saveUserId(userId: Int) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.USER_ID] = userId
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
                preferences[PreferencesKeys.USER_ID] = -1
            }
        }

        suspend fun saveMediaListSort(sort: AniMediaListSort) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.MEDIA_LIST_SORT] = sort.name
            }
        }
    }
