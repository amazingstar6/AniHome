package com.example.anilist.ui.details.mediadetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniStudio
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.models.CharacterWithVoiceActor
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.data.repository.MediaDetailsRepository
import com.example.anilist.data.repository.StudioDetailRepository
import com.example.anilist.data.repository.StudioMediaPagingSource
import com.example.anilist.data.repository.mymedia.MyMediaRepositoryImpl
import com.example.anilist.ui.details.reviewdetail.ReviewPagingSource
import com.example.anilist.ui.home.PREFETCH_DISTANCE
import com.example.anilist.ui.navigation.AniListRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// data class MediaDetailsUiState(
//    val Anime: Anime? = null,
// )

@HiltViewModel
class MediaDetailsViewModel @Inject constructor(
    private val mediaDetailsRepository: MediaDetailsRepository,
    private val myMediaRepository: MyMediaRepositoryImpl,
    private val studioDetailRepository: StudioDetailRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _media: MutableStateFlow<MediaDetailUiState> =
        MutableStateFlow(MediaDetailUiState.Loading)

    val media: StateFlow<MediaDetailUiState> = _media

    private val _mediaId = MutableStateFlow(-1)

    val selectedCharacterLanguage = MutableStateFlow(0)

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

    @OptIn(ExperimentalCoroutinesApi::class)
    val characterList: Flow<PagingData<CharacterWithVoiceActor>> =
        _mediaId.combine(selectedCharacterLanguage){id, languageId -> id to languageId }.flatMapLatest { pair ->
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
                    it.voiceActorLanguage == (media.value as? MediaDetailUiState.Success)?.data?.languages?.get(
                        selectedCharacterLanguage.value
                    )
                }
            }
                .cachedIn(viewModelScope)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val reviews = _mediaId.flatMapLatest { mediaId ->
        Pager(
            config = PagingConfig(pageSize = 25, prefetchDistance = 5, enablePlaceholders = false),
            pagingSourceFactory = {
                ReviewPagingSource(mediaDetailsRepository, mediaId)
            }
        ).flow.cachedIn(viewModelScope)
    }


    private val _character = MutableLiveData<CharacterDetail>()
    val character: LiveData<CharacterDetail> = _character

    private val _studio: MutableStateFlow<AniStudio> = MutableStateFlow(AniStudio())
    val studio: StateFlow<AniStudio> = _studio

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaOfStudio = _studio.flatMapLatest { studio ->
        Pager(
            config = PagingConfig(pageSize = 25, prefetchDistance = 5, enablePlaceholders = false),
            pagingSourceFactory = {
                StudioMediaPagingSource(studioDetailRepository, studio.id)
            }
        ).flow.cachedIn(viewModelScope)
    }

    fun getStudioDetails(id: Int) {
        viewModelScope.launch {
            _studio.value = studioDetailRepository.getStudioDetails(id)
        }
    }


    fun fetchMedia(mediaId: Int) {
        Timber.d("Saved state handle has ${savedStateHandle.get<Int>(AniListRoute.MEDIA_DETAIL_ID_KEY)}")
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

    fun toggleFavourite(type: MediaDetailsRepository.LikeAbleType, id: Int) {
        viewModelScope.launch {
            val isFavourite = mediaDetailsRepository.toggleFavourite(type, id)
            when (type) {
                MediaDetailsRepository.LikeAbleType.CHARACTER -> {}
//                    _character.value =
//                        _character.value!!.copy(isFavourite = isFavourite)

                MediaDetailsRepository.LikeAbleType.STAFF -> {}
//                    _staff.value =
//                        _staff.value!!.copy(isFavourite = isFavourite)

                MediaDetailsRepository.LikeAbleType.ANIME, MediaDetailsRepository.LikeAbleType.MANGA -> {
                    when (isFavourite) {
                        is AniResult.Failure -> {
                            //todo
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

                MediaDetailsRepository.LikeAbleType.STUDIO -> {}
//                    _studio.value = _studio.value.copy(isFavourite = isFavourite)
            }
        }
    }

    fun updateProgress(statusUpdate: StatusUpdate, mediaId: Int) {
        viewModelScope.launch {
            myMediaRepository.updateProgress(
                statusUpdate,
            )
            fetchMedia(mediaId)
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
}

sealed interface MediaDetailUiState {
    object Loading : MediaDetailUiState
    data class Success(val data: Media) : MediaDetailUiState
    data class Error(val message: String) : MediaDetailUiState
}