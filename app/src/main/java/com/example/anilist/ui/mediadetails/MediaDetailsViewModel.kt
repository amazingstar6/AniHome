package com.example.anilist.ui.mediadetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.Review
import com.example.anilist.data.models.Staff
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.data.repository.MediaDetailsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// data class MediaDetailsUiState(
//    val Anime: Anime? = null,
// )

private const val TAG = "MediaDetailsViewModel"

@HiltViewModel
class MediaDetailsViewModel @Inject constructor(
    private val mediaDetailsRepository: MediaDetailsRepository,
) : ViewModel() {

    private val _media = MutableLiveData<Media>()
    val media: LiveData<Media> = _media

//    private val _staff = Pager(
//        config = PagingConfig(pageSize = 25, enablePlaceholders = false),
//        pagingSourceFactory = { mediaDetailsRepository.pagingRepository()}
//    )

    private val _staffList = MutableLiveData<List<Staff>>()
    val staffList: LiveData<List<Staff>> = _staffList

    private val _staff = MutableLiveData<StaffDetail>()
    val staff: LiveData<StaffDetail> = _staff

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews

    private val _review = MutableLiveData<Review>()
    val review: LiveData<Review> = _review

    private val _character = MutableLiveData<CharacterDetail>()
    val character: LiveData<CharacterDetail> = _character

    private val _isFavouriteCharacter =
        MutableLiveData<Boolean>(_character.value?.isFavourite ?: false)
    val isFavouriteCharacter: LiveData<Boolean> = _isFavouriteCharacter

    fun fetchMedia(mediaId: Int) {
        viewModelScope.launch {
            val data = mediaDetailsRepository.fetchMedia(mediaId)
            _media.value = data
        }
    }

    fun fetchStaffList(mediaId: Int, page: Int) {
        viewModelScope.launch {
            val data = mediaDetailsRepository.fetchStaffList(mediaId, page)
            _staffList.value = (_staffList.value?.plus(data)) ?: data
        }
    }

    fun fetchStaff(id: Int) {
        viewModelScope.launch {
            _staff.value = mediaDetailsRepository.fetchStaff(id)
        }
    }

    fun fetchReviews(mediaId: Int) {
        viewModelScope.launch {
            val data = mediaDetailsRepository.fetchReviews(mediaId)
            _reviews.value = data
        }
    }

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

                MediaDetailsRepository.LikeAbleType.STUDIO -> Unit /* todo _studio.value = _character.value!!.copy(isFavourite = isFavourite)*/
            }

//            _character.value?.isFavorite = isFavourite
//            _isFavouriteCharacter.value = isFavourite
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

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

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
