package com.example.anilist.ui.details.characterdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.repository.CharacterDetailRepository
import com.example.anilist.data.repository.MediaDetailsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    private val characterDetailRepository: CharacterDetailRepository
): ViewModel() {
    private val _character = MutableLiveData<CharacterDetail>()
    val character: LiveData<CharacterDetail> = _character

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

    fun toggleFavourite(character: MediaDetailsRepository.LikeAbleType, id: Int) {
        TODO("Not yet implemented")
    }
}