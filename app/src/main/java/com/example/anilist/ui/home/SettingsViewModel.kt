package com.example.anilist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilist.data.repository.Theme
import com.example.anilist.data.repository.TitleFormat
import com.example.anilist.data.repository.UserDataRepository
import com.example.anilist.data.repository.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    val settingsUiState: StateFlow<SettingsUiState> =
        userDataRepository.userPreferencesFlow.map { userData ->
            SettingsUiState.Success(
                settings = userData,
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SettingsUiState.Loading,
            )


    fun saveTheme(theme: Theme) {
        viewModelScope.launch {
            userDataRepository.saveTheme(theme)
        }
    }

    fun saveTitle(titleFormat: TitleFormat) {
        viewModelScope.launch {
            userDataRepository.saveTitle(titleFormat)
        }
    }

    fun logOut() {
        viewModelScope.launch {
            userDataRepository.logOut()
        }
    }
}

sealed interface SettingsUiState {
    object Loading : SettingsUiState
    data class Success(val settings: UserSettings) : SettingsUiState
}
