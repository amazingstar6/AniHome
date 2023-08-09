package com.example.anilist.ui.mediadetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.anilist.data.models.AniStudio
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.Review
import com.example.anilist.data.models.ReviewRatingStatus
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.data.models.StatusUpdate
import com.example.anilist.data.repository.MediaDetailsRepository
import com.example.anilist.data.repository.mymedia.MyMediaRepositoryImpl
import com.example.anilist.data.repository.StudioDetailRepository
import com.example.anilist.data.repository.StudioMediaPagingSource
import com.example.anilist.ui.home.PREFETCH_DISTANCE
import com.example.anilist.ui.navigation.AniListRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// data class MediaDetailsUiState(
//    val Anime: Anime? = null,
// )

private const val TAG = "MediaDetailsViewModel"

@HiltViewModel
class MediaDetailsViewModel @Inject constructor(
    private val mediaDetailsRepository: MediaDetailsRepository,
    private val myMediaRepository: MyMediaRepositoryImpl,
    private val studioDetailRepository: StudioDetailRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    init {
        Timber.d("Init block got called!")
        savedStateHandle.get<Int>(AniListRoute.MEDIA_DETAIL_ID_KEY)?.let { fetchMedia(it) }
    }

    private val _media = MutableLiveData<Media>()
    val media: LiveData<Media> = _media

    private val _mediaId = MutableStateFlow(-1)
    val mediaId = _mediaId.asStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = -1
    )

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

    private val _staff = MutableLiveData<StaffDetail>()
    val staff: LiveData<StaffDetail> = _staff

    @OptIn(ExperimentalCoroutinesApi::class)
    val reviews = _mediaId.flatMapLatest { mediaId ->
        Pager(
            config = PagingConfig(pageSize = 25, prefetchDistance = 5, enablePlaceholders = false),
            pagingSourceFactory = {
                ReviewPagingSource(mediaDetailsRepository, mediaId)
            }
        ).flow.cachedIn(viewModelScope)
    }

    private val _review = MutableLiveData<Review>()
    val review: LiveData<Review> = _review

    private val _character = MutableLiveData<CharacterDetail>()
    val character: LiveData<CharacterDetail> = _character

    private val _studio: MutableStateFlow<AniStudio> = MutableStateFlow(AniStudio())
    val studio: StateFlow<AniStudio> = _studio

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaOfStudio = _studio.flatMapLatest {studio ->
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
            val data = mediaDetailsRepository.fetchMedia(mediaId)
            _media.value = data.getOrNull()
            _mediaId.emit(mediaId)
        }
    }

    fun fetchStaff(id: Int) {
        viewModelScope.launch {
            _staff.value = mediaDetailsRepository.fetchStaffDetail(id)
        }
    }

    fun fetchCharacter(characterId: Int) {
        viewModelScope.launch {
            _character.value = mediaDetailsRepository.fetchCharacter(characterId)
        }
    }

    fun fetchReview(reviewId: Int) {
        viewModelScope.launch {
            val data = mediaDetailsRepository.fetchReview(reviewId)
            _review.value = data
        }
    }

    fun toggleFavourite(type: MediaDetailsRepository.LikeAbleType, id: Int) {
        viewModelScope.launch {
            val isFavourite = mediaDetailsRepository.toggleFavourite(type, id)
            Log.i(TAG, "Favourite status in view model is $isFavourite")
            when (type) {
                MediaDetailsRepository.LikeAbleType.CHARACTER ->
                    _character.value =
                        _character.value!!.copy(isFavourite = isFavourite)

                MediaDetailsRepository.LikeAbleType.STAFF ->
                    _staff.value =
                        _staff.value!!.copy(isFavourite = isFavourite)

                MediaDetailsRepository.LikeAbleType.ANIME ->
                    _media.value =
                        _media.value!!.copy(isFavourite = isFavourite)

                MediaDetailsRepository.LikeAbleType.MANGA ->
                    _media.value =
                        _media.value!!.copy(isFavourite = isFavourite)

                MediaDetailsRepository.LikeAbleType.STUDIO ->
                    _studio.value = _studio.value!!.copy(isFavourite = isFavourite)
            }
        }
    }

    fun rateReview(id: Int, rating: ReviewRatingStatus) {
        viewModelScope.launch {
            mediaDetailsRepository.rateReview(id, rating)
        }
    }

    fun updateProgress(statusUpdate: StatusUpdate, mediaId: Int) {
        viewModelScope.launch {
            myMediaRepository.updateProgress(
                statusUpdate,
            )
            _media.value = mediaDetailsRepository.fetchMedia(
                mediaId
            ).getOrNull()
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            myMediaRepository.deleteEntry(id)
        }
    }
}
