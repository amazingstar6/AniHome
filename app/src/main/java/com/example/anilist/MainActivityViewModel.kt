package com.example.anilist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilist.data.repository.MainActivityRepository
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
class MainActivityViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val mainActivityRepository: MainActivityRepository
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = userDataRepository.userPreferencesFlow.map {
        MainActivityUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )

    fun saveAccessCodeAndUserId(accessCode: String, tokenType: String, expiresIn: String) {
        viewModelScope.launch {
            userDataRepository.saveAccessCode(accessCode = accessCode, tokenType = tokenType, expiresIn = expiresIn)
            val userId = mainActivityRepository.getUserId()
            userDataRepository.saveUserId(userId)
        }
    }
}

sealed interface MainActivityUiState {
    object Loading : MainActivityUiState
    data class Success(val userData: UserSettings) : MainActivityUiState
}
