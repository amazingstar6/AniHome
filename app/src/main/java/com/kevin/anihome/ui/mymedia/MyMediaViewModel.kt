package com.kevin.anihome.ui.mymedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.anihome.data.models.AniMediaListSort
import com.kevin.anihome.data.models.AniPersonalMediaStatus
import com.kevin.anihome.data.models.AniResult
import com.kevin.anihome.data.models.Media
import com.kevin.anihome.data.models.StatusUpdate
import com.kevin.anihome.data.repository.UserDataRepository
import com.kevin.anihome.data.repository.mymedia.MyMediaRepository
import com.kevin.anihome.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MyMediaViewModel
    @Inject
    constructor(
        private val myMediaRepository: MyMediaRepository,
        private val userDataRepository: UserDataRepository,
    ) : ViewModel() {
        private val _uiState: MutableStateFlow<MyMediaUiState> =
            MutableStateFlow(MyMediaUiState.Loading)
        val uiState = _uiState.asStateFlow()

        private val _toastMessage = MutableSharedFlow<String>()
        val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

        private fun sendMessage(message: String) {
            viewModelScope.launch {
                _toastMessage.emit(message)
            }
        }

        private val _sort: MutableStateFlow<AniMediaListSort> =
            MutableStateFlow(AniMediaListSort.UPDATED_TIME_DESC)

        val sort: StateFlow<AniMediaListSort> = _sort

        fun setSort(sort: AniMediaListSort) {
//        _sort.value = sort
            viewModelScope.launch {
                userDataRepository.saveMediaListSort(sort)
            }
        }

        init {
            viewModelScope.launch {
                userDataRepository.userPreferencesFlow.collectLatest {
                    _sort.value = it.mediaListSort
                }
            }
        }

        // todo remove useNetworkFirst parameter?
        fun fetchMyMedia(
            isAnime: Boolean,
            useNetworkFirst: Boolean,
        ) {
            viewModelScope.launch {
                when (val data = myMediaRepository.getMyMedia(isAnime, useNetworkFirst)) {
                    is AniResult.Success -> {
                        // notifies the user if the data is from cache when reloading
                        if (data.data.second /*boolean value notating whether cache was used*/ && useNetworkFirst) {
                            sendMessage("Network error, loaded data from cache")
                        }

                        _uiState.value = MyMediaUiState.Success(data.data.first)
                    }

                    is AniResult.Failure -> {
                        _uiState.value = MyMediaUiState.Error(data.error)
                    }
                }
            }
        }

        /**
         * Updates the progress and changes the media list only for that updated media and sorts it by updated at descending again.
         * @param isComplete if progress is equal to the amount of chapters/episodes (should only be used for increasing chapter/episode progress, since that doesn't allow to immediately set the status to COMPLETE and the end date to the current day
         */
        fun updateProgress(
            statusUpdate: StatusUpdate,
            isComplete: Boolean,
        ) {
            viewModelScope.launch {
                val newMedia =
                    myMediaRepository.updateProgress(
                        statusUpdate,
                    )
                when (newMedia) {
                    is AniResult.Success -> {
                        when (val currentList = _uiState.value) {
                            is MyMediaUiState.Error -> Unit
                            is MyMediaUiState.Loading -> Unit
                            is MyMediaUiState.Success -> {
                                val mapCopy =
                                    currentList.myMedia.mapValues { it.value.toMutableList() }
                                        .toMutableMap()

                                var mediaStatus = statusUpdate.status
                                if (isComplete) {
                                    mediaStatus = AniPersonalMediaStatus.COMPLETED
                                    newMedia.copy(
                                        data =
                                            newMedia.data.copy(
                                                mediaListEntry =
                                                    newMedia.data.mediaListEntry.copy(
                                                        status = mediaStatus,
                                                    ),
                                            ),
                                    )
                                    if (statusUpdate.completedAt == null) {
                                        newMedia.copy(
                                            data =
                                                newMedia.data.copy(
                                                    mediaListEntry =
                                                        newMedia.data.mediaListEntry.copy(
                                                            completedAt = Utils.getCurrentDay(),
                                                        ),
                                                ),
                                        )
                                    }
                                }

                                Timber.d(
                                    "Media status before moving locally is $mediaStatus, is complete is $isComplete, media status received is ${newMedia.data.mediaListEntry.status}",
                                )
                                // first we move the status if we need to
                                if (mediaStatus != null) {
                                    // first we remove the current entry from the wrong list
                                    mapCopy.forEach {
                                        val indexToRemove =
                                            it.value.indexOfFirst { value -> value.mediaListEntry.listEntryId == statusUpdate.entryListId }
                                        if (indexToRemove != -1) {
                                            it.value.removeAt(indexToRemove)
                                        }
                                    }
                                    // then we add the new media
                                    if (mapCopy[mediaStatus] != null) {
                                        mapCopy[mediaStatus]!!.add(newMedia.data)
                                    } else {
                                        mapCopy[mediaStatus] = mutableListOf(newMedia.data)
                                    }
                                }

                                mapCopy.forEach {
                                    val indexToReplace =
                                        it.value.indexOfFirst { value -> value.mediaListEntry.listEntryId == statusUpdate.entryListId }
                                    if (indexToReplace != -1) {
                                        it.value[indexToReplace] = newMedia.data
                                    }
                                }

                                // sorting every list afterwards in case any media got moved around from status
                                mapCopy.forEach {
                                    it.value.sortByDescending { media ->
                                        sortDate(media.mediaListEntry.updatedAt)
                                    }
                                }
                                _uiState.value = MyMediaUiState.Success(mapCopy)
                            }
                        }
                    }

                    is AniResult.Failure -> {
                        sendMessage("Updating progress failed, please try again")
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
//                _uiState.value = MyMediaUiState.Error("Deleting entry failed, please try again")
                    sendMessage("Deletion failed, please try again")
                } else {
                    when (val currentList = _uiState.value) {
                        is MyMediaUiState.Error -> Unit
                        is MyMediaUiState.Loading -> Unit
                        is MyMediaUiState.Success -> {
                            val mapCopy = currentList.myMedia.mapValues { it.value.toMutableList() }
                            mapCopy.forEach {
                                val indexToRemove =
                                    it.value.indexOfFirst { value -> value.mediaListEntry.listEntryId == entryListId }
                                if (indexToRemove != -1) {
                                    it.value.removeAt(indexToRemove)
                                }
                                it.value.sortByDescending { media ->
                                    sortDate(media.mediaListEntry.updatedAt)
                                }
                            }
                            _uiState.value = MyMediaUiState.Success(mapCopy)
                        }
                    }
                }
            }
        }
    }

sealed interface MyMediaUiState {
    object Loading : MyMediaUiState

    data class Success(val myMedia: Map<AniPersonalMediaStatus, List<Media>>) : MyMediaUiState

    data class Error(val message: String) : MyMediaUiState
}
