package com.example.anilist.ui.details.mediadetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.anilist.data.models.AniLikeAbleType
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniReviewRatingStatus
import com.example.anilist.data.models.CharacterWithVoiceActor
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.data.repository.ReviewDetailRepository
import com.example.anilist.data.repository.mediadetail.CharacterPagingSource
import com.example.anilist.data.repository.mediadetail.MediaDetailsRepository
import com.example.anilist.data.repository.mediadetail.StaffPagingSource
import com.example.anilist.data.repository.mymedia.MyMediaRepository
import com.example.anilist.data.repository.mediadetail.ReviewPagingSource
import com.example.anilist.ui.home.PREFETCH_DISTANCE
import com.example.anilist.ui.navigation.AniListRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// data class MediaDetailsUiState(
//    val Anime: Anime? = null,
// )

//todo refactor repositories?
@HiltViewModel
class MediaDetailsViewModel @Inject constructor(
    private val mediaDetailsRepository: MediaDetailsRepository,
    private val myMediaRepository: MyMediaRepository,
    private val reviewDetailRepository: ReviewDetailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _media: MutableStateFlow<MediaDetailUiState> =
        MutableStateFlow(MediaDetailUiState.Loading)

    val media: StateFlow<MediaDetailUiState> = _media

    private val _mediaId = MutableStateFlow(-1)

    val selectedCharacterLanguage = MutableStateFlow(0)

    private val _toast = MutableSharedFlow<String>()
    val toast = _toast.asSharedFlow()

    private fun sendMessage(text: String) {
        viewModelScope.launch {
            _toast.emit(text)
        }
    }

    init {
        savedStateHandle.get<Int>(AniListRoute.MEDIA_DETAIL_ID_KEY)?.let {
            _mediaId.value = it
            fetchMedia(it)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val staffList = _mediaId.flatMapLatest { mediaId ->
        Pager(
            config = PagingConfig(
                pageSize = 25,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StaffPagingSource(mediaId, mediaDetailsRepository) }
        ).flow.cachedIn(viewModelScope)
    }

    /**
     * This should get loaded only when the media has loaded as well,
     * the get or else is only there to avoid exceptions
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val characterList: Flow<PagingData<CharacterWithVoiceActor>> =
        _mediaId.combine(selectedCharacterLanguage) { id, languageId -> id to languageId }
            .flatMapLatest { pair ->
                Pager(
                    config = PagingConfig(
                        pageSize = 25,
                        prefetchDistance = PREFETCH_DISTANCE,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = {
                        CharacterPagingSource(
                            mediaId = pair.first,
                            mediaDetailsRepository = mediaDetailsRepository
                        )
                    }
                ).flow.map { pagingData ->
                    pagingData.filter {
//                        if ((media.value as? MediaDetailUiState.Success)?.data?.languages != null) {
//                            it.voiceActorLanguage == (media.value as? MediaDetailUiState.Success)?.data?.languages?.get(
//                                selectedCharacterLanguage.value
//                            )
//                        } else {
//                            true
//                        }
                        it.voiceActorLanguage == (media.value as? MediaDetailUiState.Success)?.data?.languages?.getOrElse(
                            selectedCharacterLanguage.value
                        ) { "Japanese" }
                    }
                }
                    .cachedIn(viewModelScope)
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    val reviews = _mediaId.flatMapLatest { mediaId ->
        Timber.d("Paging reviews?")
        Pager(
            config = PagingConfig(pageSize = 25, prefetchDistance = 5, enablePlaceholders = false),
            pagingSourceFactory = {
                ReviewPagingSource(mediaDetailsRepository, mediaId)
            }
        ).flow.cachedIn(viewModelScope)
    }


    fun fetchMedia(mediaId: Int) {
        viewModelScope.launch {
            when (val data = mediaDetailsRepository.fetchMedia(mediaId)) {
                is AniResult.Success -> {
                    _media.value = MediaDetailUiState.Success(data.data)
                    _mediaId.emit(mediaId)
                }

                is AniResult.Failure -> {
                    _media.value = MediaDetailUiState.Error(data.error)
                }
            }

        }
    }

    fun toggleFavourite(type: AniLikeAbleType, id: Int) {
        viewModelScope.launch {
            val isFavourite = mediaDetailsRepository.toggleFavourite(type, id)
            when (type) {
                AniLikeAbleType.CHARACTER -> {}
//                    _character.value =
//                        _character.value!!.copy(isFavourite = isFavourite)

                AniLikeAbleType.STAFF -> {}
//                    _staff.value =
//                        _staff.value!!.copy(isFavourite = isFavourite)

                AniLikeAbleType.ANIME, AniLikeAbleType.MANGA -> {
                    when (isFavourite) {
                        is AniResult.Failure -> {
                            sendMessage(isFavourite.error)
                        }

                        is AniResult.Success -> {
                            if (_media.value is MediaDetailUiState.Success) _media.value =
                                MediaDetailUiState.Success(
                                    (_media.value as MediaDetailUiState.Success).data.copy(
                                        isFavourite = isFavourite.data
                                    )
                                )
                        }
                    }
                }

                AniLikeAbleType.STUDIO -> {}
//                    _studio.value = _studio.value.copy(isFavourite = isFavourite)
            }
        }
    }

    fun updateProgress(statusUpdate: StatusUpdate) {
        viewModelScope.launch {
            when (val data = myMediaRepository.updateProgress(
                statusUpdate,
            )) {
                is AniResult.Success -> {
                    _media.value = MediaDetailUiState.Success(
                        (_media.value as MediaDetailUiState.Success).data.copy(
                            mediaListEntry = data.data.mediaListEntry
                        )
                    )
//                    _media.value = MediaDetailUiState.Success(data.data)
                }

                is AniResult.Failure -> {
                    sendMessage(data.error)
//                    sendMessage("Failed updating progress, please try again")
                }
            }
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            myMediaRepository.deleteEntry(id)
        }
    }

    fun setCharacterLanguage(language: Int) {
        selectedCharacterLanguage.value = language
    }

    fun rateReview(reviewId: Int, rating: AniReviewRatingStatus) {
        Timber.d("Rating review $reviewId with $rating")
        viewModelScope.launch {
            reviewDetailRepository.rateReview(id = reviewId, rating = rating)
        }
    }
}


sealed interface MediaDetailUiState {
    object Loading : MediaDetailUiState
    data class Success(val data: Media) : MediaDetailUiState
    data class Error(val message: String) : MediaDetailUiState
}