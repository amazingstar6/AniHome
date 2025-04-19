package com.example.anilist

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.repository.MainActivityRepository
import com.example.anilist.data.repository.UserDataRepository
import com.example.anilist.data.repository.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel
    @Inject
    constructor(
        private val userDataRepository: UserDataRepository,
        private val mainActivityRepository: MainActivityRepository,
    ) : ViewModel() {
        val uiState: StateFlow<MainActivityUiState> =
            userDataRepository.userPreferencesFlow.map {
                MainActivityUiState.Success(it)
            }.stateIn(
                scope = viewModelScope,
                initialValue = MainActivityUiState.Loading,
                started = SharingStarted.WhileSubscribed(5_000),
            )

        private val _toastMessage = MutableSharedFlow<String>()
        val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

        private fun sendMessage(message: String) {
            viewModelScope.launch {
                _toastMessage.emit(message)
            }
        }

        fun processIntentUti(data: Uri?) {
            if (data != null) {
                Timber.i("Data is " + data)
                val uri = data.toString()
                val query = uri.substringAfter("#")
                val parameters = query.split("&")
                val parameterMap =
                    parameters.associate {
                        val (key, value) = it.split("=")
                        key to value
                    }
                MainActivity.accessCode = parameterMap["access_token"] ?: ""
                val tokenType = parameterMap["token_type"] ?: ""
                val expiresIn = parameterMap["expires_in"] ?: ""
                Timber.i(
                    "Access code is ${MainActivity.accessCode}, token type is $tokenType, expires in $expiresIn seconds",
                )
                saveAccessCodeAndUserId(MainActivity.accessCode, tokenType, expiresIn)
            }
        }

        private fun saveAccessCodeAndUserId(
            accessCode: String,
            tokenType: String,
            expiresIn: String,
        ) {
            viewModelScope.launch {
                userDataRepository.saveAccessCode(
                    accessCode = accessCode,
                    tokenType = tokenType,
                    expiresIn = expiresIn,
                )
                when (val resultId = mainActivityRepository.getUserId(accessCode)) {
                    is AniResult.Success -> {
                        userDataRepository.saveUserId(resultId.data)
                    }

                    is AniResult.Failure -> {
                        _toastMessage.emit("Something went wrong while logging in, please try again")
                        userDataRepository.logOut()
                    }
                }
            }
        }
    }

sealed interface MainActivityUiState {
    object Loading : MainActivityUiState

    data class Success(val userData: UserSettings) : MainActivityUiState
}
