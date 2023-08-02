package com.example.anilist.ui.mymedia

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.data.repository.MyMediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MyMediaViewModel"

@HiltViewModel
class MyMediaViewModel @Inject constructor(
    private val myMediaRepository: MyMediaRepository,
) : ViewModel() {
    private val _myAnime = MutableLiveData<Map<MediaStatus, List<Media>>>()
    val myAnime: MutableLiveData<Map<MediaStatus, List<Media>>> = _myAnime
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

    fun updateProgress(
        statusUpdate: StatusUpdate,
        isAnime: Boolean
    ) {
        viewModelScope.launch {
            val newMedia: Media = myMediaRepository.updateProgress(
                statusUpdate,
            )
            val data = myMediaRepository.getMyMedia(isAnime)
            if (isAnime) {
//                _myAnime.value = data

                val mapCopy = _myAnime.value?.mapValues { it.value.toMutableList() }
                mapCopy?.forEach {
                    val indexToReplace =
                        it.value.indexOfFirst { it.listEntryId == statusUpdate.entryListId }
                    if (indexToReplace != -1) {
                        it.value[indexToReplace] = newMedia
                    }

                }
                mapCopy.let {
                    _myAnime.value = it
                }
            } else {
                _myManga.value = data
            }
        }
    }

    fun deleteEntry(id: Int, isAnime: Boolean) {
        viewModelScope.launch {
            myMediaRepository.deleteEntry(id)
            val data = myMediaRepository.getMyMedia(isAnime)
            if (isAnime) {
                _myAnime.value = data
            } else {
                _myManga.value = data
            }
        }
    }

//    fun increaseEpisodeProgress(mediaId: Int, newProgress: Int) {
//        Log.i(TAG, "Episode progress is being increased in view model")
//        viewModelScope.launch {
//            myMediaRepository.increaseEpisodeProgress(mediaId, newProgress)
//        }
//    }
}
