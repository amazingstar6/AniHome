package com.example.anilist.ui.details.characterdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilist.data.models.AniCharacterDetail
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.repository.CharacterDetailRepository
import com.example.anilist.ui.navigation.AniListRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CharacterDetailViewModel
    @Inject
    constructor(
        private val characterDetailRepository: CharacterDetailRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val _character: MutableStateFlow<CharacterDetailUiState> =
            MutableStateFlow(CharacterDetailUiState.Loading)
        val character: StateFlow<CharacterDetailUiState> = _character

        private val _toast = MutableSharedFlow<String>()
        val toast = _toast.asSharedFlow()

        init {
            Timber.d("Init block got called in character detail view model")
            fetchCharacter(savedStateHandle.get<Int>(AniListRoute.CHARACTER_DETAIL_KEY) ?: -1)
        }

        fun fetchCharacter(characterId: Int) {
            viewModelScope.launch {
                when (val data = characterDetailRepository.fetchCharacter(characterId)) {
                    is AniResult.Success -> {
                        _character.value = CharacterDetailUiState.Success(data.data)
                    }

                    is AniResult.Failure -> {
                        _character.value = CharacterDetailUiState.Error(data.error)
                    }
                }
            }
        }

        fun toggleFavourite(id: Int) {
            viewModelScope.launch {
                when (val data = characterDetailRepository.toggleFavourite(id)) {
                    is AniResult.Failure -> {
                        _toast.emit(data.error)
                    }

                    is AniResult.Success -> {
                        when (_character.value) {
                            is CharacterDetailUiState.Error -> {}
                            CharacterDetailUiState.Loading -> {}
                            is CharacterDetailUiState.Success -> {
                                (_character.value as CharacterDetailUiState.Success).character.let {
                                    _character.value =
                                        CharacterDetailUiState.Success(it.copy(isFavourite = data.data))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

sealed interface CharacterDetailUiState {
    object Loading : CharacterDetailUiState

    data class Error(val message: String) : CharacterDetailUiState

    data class Success(val character: AniCharacterDetail) : CharacterDetailUiState
}
