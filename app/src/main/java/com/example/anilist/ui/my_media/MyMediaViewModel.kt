package com.example.anilist.ui.my_media

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.data.repository.MyMediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MyMediaViewModel"

@HiltViewModel
class MyMediaViewModel @Inject constructor(
    private val myMediaRepository: MyMediaRepository
) : ViewModel() {
    private val _myAnime = MutableLiveData<Map<MediaStatus, List<Media>>>()
    val myAnime: LiveData<Map<MediaStatus, List<Media>>> = _myAnime
    private val _myManga = MutableLiveData<Map<MediaStatus, List<Media>>>()
    val myManga: LiveData<Map<MediaStatus, List<Media>>> = _myManga

    fun fetchMyMedia(isAnime: Boolean) {
        viewModelScope.launch {
            val data = myMediaRepository.getMyMedia(isAnime)
            if (isAnime) {
                _myAnime.value = data
            } else {
                _myManga.value = data
            }
        }
    }
    fun increaseEpisodeProgress(mediaId: Int, newProgress: Int) {
        Log.i(TAG, "Episode progress is being increased in view model")
        viewModelScope.launch {
            myMediaRepository.increaseEpisodeProgress(mediaId, newProgress)
        }
    }

    fun updateProgress(
        statusUpdate: StatusUpdate
    ) {
        Log.d(TAG, "Progress is being updated in view model!")
        viewModelScope.launch {
            myMediaRepository.updateProgress(
                statusUpdate
            )
        }
    }

}