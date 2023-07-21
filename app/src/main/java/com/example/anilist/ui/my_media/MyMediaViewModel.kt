package com.example.anilist.ui.my_media

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anilist.data.repository.MyMediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MyMediaViewModel"

@HiltViewModel
class MyMediaViewModel @Inject constructor(
    private val myMediaRepository: MyMediaRepository
) : ViewModel() {
    var myAnime = myMediaRepository.getMyMedia(isAnime = true).asLiveData()
    val myManga = myMediaRepository.getMyMedia(isAnime = false).asLiveData()

    fun increaseEpisodeProgress(mediaId: Int, newProgress: Int) {
        Log.i(TAG, "Episode progress is being increased in view model")
        viewModelScope.launch {
            myMediaRepository.increaseEpisodeProgress(mediaId, newProgress)
        }
    }

    fun refreshAnime() {
        viewModelScope.launch {
            myAnime = myMediaRepository.getMyMedia(true).asLiveData()
        }
    }

    fun refreshManga() {
        viewModelScope.launch {
            myMediaRepository.getMyMedia(false)
        }
    }

    init {

    }

}