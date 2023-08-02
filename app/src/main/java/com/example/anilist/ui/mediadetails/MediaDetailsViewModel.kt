package com.example.anilist.ui.mediadetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.example.anilist.data.repository.MyMediaRepository
import com.example.anilist.data.repository.StudioDetailRepository
import com.example.anilist.data.repository.StudioMediaPagingSource
import com.example.anilist.ui.home.PREFETCH_DISTANCE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// data class MediaDetailsUiState(
//    val Anime: Anime? = null,
// )

private const val TAG = "MediaDetailsViewModel"

@HiltViewModel
class MediaDetailsViewModel @Inject constructor(
    private val mediaDetailsRepository: MediaDetailsRepository,
    private val myMediaRepository: MyMediaRepository,
    private val studioDetailRepository: StudioDetailRepository
) : ViewModel() {

    private val _media = MutableLiveData<Media>()
    val media: LiveData<Media> = _media

    private val _mediaId = MutableStateFlow(-1)
    val mediaId = _mediaId.asStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = -1
    )

    //    private val _staff = Pager(
//        config = PagingConfig(pageSize = 25, enablePlaceholders = false),
//        pagingSourceFactory = { mediaDetailsRepository.pagingRepository()}
//    )

    //    private val _staffList = MutableLiveData<List<Staff>>()
//    val staffList: LiveData<List<Staff>> = _staffList
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

//    private val _studio = MutableLiveData<StaffDetail>()
//    val studio: LiveData<StaffDetail> = _studio

    //    private val _reviews = MutableLiveData<List<Review>>()
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

    private val _isFavouriteCharacter =
        MutableLiveData(_character.value?.isFavourite ?: false)
    val isFavouriteCharacter: LiveData<Boolean> = _isFavouriteCharacter

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
        viewModelScope.launch {
            val data = mediaDetailsRepository.fetchMedia(mediaId)
            _media.value = data.getOrNull()
            _mediaId.emit(mediaId)
        }
    }

//    fun fetchStaffList(mediaId: Int, page: Int) {
//        viewModelScope.launch {
//            val data = mediaDetailsRepository.fetchStaffList(mediaId, page)
//            _staffList.value = (_staffList.value?.plus(data)) ?: data
//        }
//    }

    fun fetchStaff(id: Int) {
        viewModelScope.launch {
            _staff.value = mediaDetailsRepository.fetchStaffDetail(id)
        }
    }

//    fun fetchReviews(mediaId: Int) {
//        viewModelScope.launch {
//            val data = mediaDetailsRepository.fetchReviews(mediaId)
//            _reviews.value = data
//        }
//    }

    fun fetchCharacter(characterId: Int) {
        viewModelScope.launch {
            _character.value = mediaDetailsRepository.fetchCharacter(characterId)
        }
    }

//    fun fetchStats(mediaId: Int) {
//        viewModelScope.launch {
//            _stats.value = mediaDetailsRepository.fetchStats(mediaId)
//        }
//    }

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

    fun updateProgress(statusUpdate: StatusUpdate) {
        viewModelScope.launch {
            myMediaRepository.updateProgress(
                statusUpdate,
            )
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            myMediaRepository.deleteEntry(id)
        }
    }

//    private val _media: MutableLiveData<Media> by lazy {
//        MutableLiveData<Media>().apply {
// //            if (cacheList.any { it.id == _mediaId.value }) {
// //                _media.value = cacheList.find { it.id == _mediaId.value }
// //                Log.i(TAG, "We just used some cache! #4")
// //            } else {
//            viewModelScope.launch {
//                val response =
//                    apolloClient.query(GetAnimeInfoQuery(_mediaId.value ?: 0)).execute()
// //                    val media = Anime(
// //                        id = _mediaId.value ?: 0,
// //                        title = response.data?.Media?.title?.native ?: ""
// //                    )
//                val media = parseMedia(response.data?.Media)
// //                    cacheList.add(media)
//                _media.value = media
//                Log.i(
//                    TAG,
//                    "We did not use cache for media with id: ${_mediaId.value} #5; $cacheList"
//                )
// //                }
//            }
//        }
//    }

//
//    private val _media: MutableLiveData<Anime> = _mediaId.switchMap {
//        it ->
//
//    }

//    private val _dataLoading = MutableLiveData<Boolean>()
//    val dataLoading: LiveData<Boolean> = _dataLoading

//    fun start(mediaId: Int) {
//        // If we're already loading or already loaded, return (might be a config change)
//        if (_dataLoading.value == true || mediaId == _mediaId.value) {
//            return
//        }
//        // Trigger the load
//        _mediaId.value = mediaId
//        viewModelScope.launch {
//            val response =
//                apolloClient.query(GetAnimeInfoQuery(_mediaId.value ?: 0)).execute()
//            _media.value = parseMedia(response.data?.Media)
//        }
//    }
//
//    fun refresh() {
//        _media.value?.let {
//            _dataLoading.value = true
//            viewModelScope.launch {
//                val response =
//                    ApolloClient.Builder().serverUrl("https://graphql.anilist.co").build()
//                        .query(GetAnimeInfoQuery(_mediaId.value ?: 0)).execute()
//                _media.value = Media(title = response.data?.Media?.title?.native ?: "", note = "")
//                _dataLoading.value = false
//            }
//        }
//    }
}
