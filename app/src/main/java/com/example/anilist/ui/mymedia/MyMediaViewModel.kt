package com.example.anilist.ui.mymedia

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.data.repository.MyMediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MyMediaViewModel"

@HiltViewModel
class MyMediaViewModel @Inject constructor(
    private val myMediaRepository: MyMediaRepository,
) : ViewModel() {

    private val _uiState: MutableStateFlow<MyMediaUiState> =
        MutableStateFlow(MyMediaUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun fetchMyMedia(isAnime: Boolean) {
        Log.d(TAG, "Fetch media got called")
        viewModelScope.launch {
            when (val data = myMediaRepository.getMyMedia(isAnime)) {
                is AniResult.Success -> {
                    _uiState.value = MyMediaUiState.Success(data.data)
                }

                is AniResult.Failure -> {
                    _uiState.value = MyMediaUiState.Error(data.error)
                }
            }


        }
    }

    /**
     * Updates the progress and changes the media list only for that updated media and sorts it by updated at descending again.
     */
    fun updateProgress(
        statusUpdate: StatusUpdate
    ) {
        viewModelScope.launch {
            val newMedia = myMediaRepository.updateProgress(
                statusUpdate,
            )
            when (newMedia) {
                is AniResult.Success -> {
                    when (val currentList = _uiState.value) {
                        is MyMediaUiState.Error -> Unit
                        is MyMediaUiState.Loading -> Unit
                        is MyMediaUiState.Success -> {
                            val mapCopy = currentList.myMedia.mapValues { it.value.toMutableList() }
                                .toMutableMap()

                            // first we move the status if we need to
                            if (statusUpdate.status != null) {
                                // first we remove the current entry from the wrong list
                                mapCopy.forEach {
                                    val indexToRemove =
                                        it.value.indexOfFirst { value -> value.listEntryId == statusUpdate.entryListId }
                                    if (indexToRemove != -1) {
                                        it.value.removeAt(indexToRemove)
                                    }
                                }
                                // then we add the new media
                                if (mapCopy[statusUpdate.status] != null) {
                                    mapCopy[statusUpdate.status]!!.add(newMedia.data)
                                } else {
                                    mapCopy[statusUpdate.status] = mutableListOf(newMedia.data)
                                }
                            }

                            mapCopy.forEach {
                                val indexToReplace =
                                    it.value.indexOfFirst { value -> value.listEntryId == statusUpdate.entryListId }
                                if (indexToReplace != -1) {
                                    it.value[indexToReplace] = newMedia.data
                                }
                            }

                            // sorting the list afterwards in case any media got moved around from status
                            mapCopy.forEach {
                                it.value.sortByDescending { media ->
                                    media.updatedAt
                                }
                            }
                            _uiState.value = MyMediaUiState.Success(mapCopy)
                        }
                    }
                }

                is AniResult.Failure -> {
                    _uiState.value = MyMediaUiState.Error(newMedia.error)
                }

            }
        }
    }

    /**
     * Deletes an entry by its entry list id and if the deletion was successful, it removes it locally as well
     */
    fun deleteEntry(entryListId: Int) {
        viewModelScope.launch {
            val deletionIsSuccessful = myMediaRepository.deleteEntry(entryListId)
            if (!deletionIsSuccessful) {
                _uiState.value = MyMediaUiState.Error("Deleting entry failed, please try again")
            }

            when (val currentList = _uiState.value) {
                is MyMediaUiState.Error -> Unit
                is MyMediaUiState.Loading -> Unit
                is MyMediaUiState.Success -> {
                    val mapCopy = currentList.myMedia.mapValues { it.value.toMutableList() }
                    mapCopy.forEach {
                        val indexToRemove =
                            it.value.indexOfFirst { value -> value.listEntryId == entryListId }
                        if (indexToRemove != -1) {
                            it.value.removeAt(indexToRemove)
                        }
                        it.value.sortByDescending { media ->
                            media.updatedAt
                        }
                    }
                    _uiState.value = MyMediaUiState.Success(mapCopy)
                }
            }
        }
    }
}


sealed interface MyMediaUiState {
    object Loading : MyMediaUiState
    data class Success(val myMedia: Map<PersonalMediaStatus, List<Media>>) : MyMediaUiState
    data class Error(val message: String) : MyMediaUiState
}