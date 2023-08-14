package com.example.anilist.ui.details.characterdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniCharacterDetail
import com.example.anilist.data.repository.CharacterDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    private val characterDetailRepository: CharacterDetailRepository
) : ViewModel() {
    private val _character = MutableLiveData<AniCharacterDetail>() // todo change into ui state sealed interface?
    val character: LiveData<AniCharacterDetail> = _character

    fun fetchCharacter(characterId: Int) {
        viewModelScope.launch {
            when (val data = characterDetailRepository.fetchCharacter(characterId)) {
                is AniResult.Success -> {
                    _character.value = data.data
                }

                is AniResult.Failure -> {

                }
            }

        }
    }

    fun toggleFavourite(id: Int) {
        viewModelScope.launch {
            when (val data = characterDetailRepository.toggleFavourite(id)) {
                is AniResult.Failure -> {
                    //todo send toast
                }

                is AniResult.Success -> {
                    _character.value = _character.value?.copy(isFavourite = data.data)
                }
            }
        }
    }
}