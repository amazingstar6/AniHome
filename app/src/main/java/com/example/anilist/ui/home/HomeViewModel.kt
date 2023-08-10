package com.example.anilist.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.anilist.data.models.AniMediaStatus
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniStudio
import com.example.anilist.data.models.AniTag
import com.example.anilist.data.models.AniThread
import com.example.anilist.data.models.AniUser
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.Season
import com.example.anilist.data.models.StaffDetail
import com.example.anilist.data.repository.HomeMedia
import com.example.anilist.data.repository.HomeRepository
import com.example.anilist.data.repository.NotificationRepository
import com.example.anilist.data.repository.TrendingTogether
import com.example.anilist.ui.home.searchpagingsource.SearchCharactersPagingSource
import com.example.anilist.ui.home.searchpagingsource.SearchMediaPagingSource
import com.example.anilist.ui.home.searchpagingsource.SearchStaffPagingSource
import com.example.anilist.ui.home.searchpagingsource.SearchStudioPagingSource
import com.example.anilist.ui.home.searchpagingsource.SearchThreadPagingSource
import com.example.anilist.ui.home.searchpagingsource.SearchUserPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val TIME_OUT = 300L //milli seconds
private const val PAGE_SIZE = 25
const val PREFETCH_DISTANCE = 10

data class MediaSearchState(
    val query: String,
    val searchType: SearchFilter,
    val sort: AniMediaSort,
    val currentSeason: Season,
    val status: AniMediaStatus,
    val year: Int,
    val genres: List<String>,
    val tags: List<String>
)

data class CharacterSearchState(
    val query: String,
    val sort: AniCharacterSort
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    notificationRepository: NotificationRepository,
    private val homeRepository: HomeRepository,
) :
    ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _trendingUiState = MutableStateFlow(HomeUiState.Loading)
    val trendingUiState: StateFlow<HomeUiState> = _trendingUiState

    private var _isAnime = MutableStateFlow(true)
    val isAnime: StateFlow<Boolean> = _isAnime

    private var _tags: MutableStateFlow<List<AniTag>> = MutableStateFlow(emptyList())
    val tags: StateFlow<List<AniTag>> = _tags

    private var _genres: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val genres: StateFlow<List<String>> = _genres

    fun setToAnime() {
        _isAnime.value = true
    }

    fun setToManga() {
        Timber.d("Set to manga was called")
        _isAnime.value = false
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val trendingTogetherPager: Flow<PagingData<TrendingTogether>> =
        isAnime.flatMapLatest {
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    prefetchDistance = PREFETCH_DISTANCE,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { homeRepository.trendingTogetherPagingSource(isAnime = isAnime.value) }
            ).flow.cachedIn(viewModelScope)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val trendingNowPager: Flow<PagingData<Media>> =
        isAnime.flatMapLatest {
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    prefetchDistance = PREFETCH_DISTANCE,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { homeRepository.trendingNowPagingSource(isAnime = isAnime.value) }
            ).flow.cachedIn(viewModelScope)
        }

    val popularThisSeasonPager =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { homeRepository.popularThisSeasonPagingSource() }
        ).flow.cachedIn(viewModelScope)

    val upComingNextSeasonPager = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = PREFETCH_DISTANCE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { homeRepository.upcomingNextSeasonPagingSource() }
    ).flow.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val allTimePopularPager =
        isAnime.flatMapLatest {
            Pager(
                config = PagingConfig(
                    pageSize = 25,
                    prefetchDistance = PREFETCH_DISTANCE,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { homeRepository.allTimePopularPagingSource(isAnime = isAnime.value) }
            ).flow.cachedIn(viewModelScope)
        }


    @OptIn(ExperimentalCoroutinesApi::class)
    val top100AnimePager = isAnime.flatMapLatest {
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { homeRepository.top100AnimePagingSource(isAnime = isAnime.value) }
        ).flow.cachedIn(viewModelScope)
    }

    val popularManhwaPager = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = PREFETCH_DISTANCE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { homeRepository.popularManhwaPagingSource() }
    ).flow.cachedIn(viewModelScope)


    private val _search = MutableStateFlow("")
    val search = _search.asStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = ""
        )
    private val _mediaSearch = MutableStateFlow("")
    private val _characterSearch = MutableStateFlow("")
    private val _staffSearch = MutableStateFlow("")
    private val _studioSearch = MutableStateFlow("")
    private val _threadSearch = MutableStateFlow("")
    private val _userSearch = MutableStateFlow("")

    private val _searchType = MutableStateFlow(SearchFilter.MEDIA)
    val searchType = _searchType.asStateFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        initialValue = SearchFilter.MEDIA
    )
    private val _mediaSortType = MutableStateFlow(AniMediaSort.POPULARITY)
    val mediaSortType = _mediaSortType.asStateFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        initialValue = AniMediaSort.SEARCH_MATCH
    )
    private val _characterSortType = MutableStateFlow(AniCharacterSort.DEFAULT)
    val characterSortType = _characterSortType.asStateFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        initialValue = AniCharacterSort.DEFAULT
    )

    private val mediaSearchState = MutableStateFlow(
        MediaSearchState(
            query = "",
            searchType = SearchFilter.MEDIA,
            sort = AniMediaSort.POPULARITY,
            currentSeason = Season.UNKNOWN,
            status = AniMediaStatus.UNKNOWN,
            year = -1,
            genres = emptyList(),
            tags = emptyList()
        )
    )


    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchResultsMedia =
        mediaSearchState.debounce(TIME_OUT).flatMapLatest { searchState ->
            Timber.d("Media search is searching for " + searchState.query)
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    prefetchDistance = 5,
                    enablePlaceholders = true
                ),
                pagingSourceFactory = {
                    //todo convert parameters to searchstate data class
                    SearchMediaPagingSource(
                        homeRepository = homeRepository,
                        search = searchState.query,
                        mediaSearchType = searchState.searchType,
                        sortType = searchState.sort,
                        season = searchState.currentSeason,
                        status = searchState.status,
                        year = searchState.year,
                        genres = searchState.genres,
                        tags = searchState.tags
                    )
                }
            ).flow.cachedIn(viewModelScope)
        }
//        combine(
//            _mediaSearch,
//            searchType,
//            mediaSortType,
//            season,
//            status
//        ) { query, searchType, sortType, currentSeason, status ->
//            MediaSearchState(
//                query = query,
//                searchType = searchType,
//                sort = sortType,
//                currentSeason = currentSeason,
//                status = status
//            )
//        }
//            .debounce(300).flatMapLatest { searchState ->
//                Timber.d("Media search is searching for " + searchState.query)
//                Pager(
//                    config = PagingConfig(
//                        pageSize = PAGE_SIZE,
//                        prefetchDistance = 5,
//                        enablePlaceholders = true
//                    ),
//                    pagingSourceFactory = {
//                        SearchMediaPagingSource(
//                            homeRepository = homeRepository,
//                            search = searchState.query,
//                            mediaSearchType = searchState.searchType,
//                            sortType = searchState.sort,
//                            season = searchState.currentSeason,
//                            status = searchState.status
//                        )
//                    }
//                ).flow.cachedIn(viewModelScope)
//            }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResultsCharacter: Flow<PagingData<CharacterDetail>> =
        combine(_characterSearch, characterSortType) { query, sortType ->
            CharacterSearchState(query = query, sort = sortType)
        }
            .debounce(300).flatMapLatest { searchState ->
                Timber.d("Character search is searching for " + searchState.query + " with sort " + searchState.sort)
                Pager(
                    config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        prefetchDistance = 5,
                        enablePlaceholders = true
                    ),
                    pagingSourceFactory = {
                        SearchCharactersPagingSource(
                            homeRepository = homeRepository,
                            search = searchState.query,
                            sortType = searchState.sort
                        )
                    }
                ).flow.cachedIn(viewModelScope)
            }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResultsStaff: Flow<PagingData<StaffDetail>> =
        _staffSearch.debounce(300).flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    prefetchDistance = 5,
                    enablePlaceholders = true
                ),
                pagingSourceFactory = {
                    SearchStaffPagingSource(
                        homeRepository = homeRepository,
                        search = query,
                    )
                }
            ).flow.cachedIn(viewModelScope)
        }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchResultsStudio: Flow<PagingData<AniStudio>> = _studioSearch.debounce(
        TIME_OUT
    ).flatMapLatest { query ->
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 5,
                enablePlaceholders = true
            ),
            pagingSourceFactory = {
                SearchStudioPagingSource(
                    homeRepository = homeRepository,
                    search = query,
                )
            }
        ).flow.cachedIn(viewModelScope)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResultsThread: Flow<PagingData<AniThread>> =
        _threadSearch.debounce(TIME_OUT).flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    prefetchDistance = 5,
                    enablePlaceholders = true
                ),
                pagingSourceFactory = {
                    SearchThreadPagingSource(
                        homeRepository = homeRepository,
                        search = query,
                    )
                }
            ).flow.cachedIn(viewModelScope)
        }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResultsUser: Flow<PagingData<AniUser>> =
        _userSearch.debounce(TIME_OUT).flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    prefetchDistance = 5,
                    enablePlaceholders = true
                ),
                pagingSourceFactory = {
                    SearchUserPagingSource(
                        homeRepository = homeRepository,
                        search = query,
                    )
                }
            ).flow.cachedIn(viewModelScope)
        }

    fun setSearch(
        query: String,
        searchType: SearchFilter,
        mediaSort: AniMediaSort,
        season: Season,
        status: AniMediaStatus,
        year: Int,
        selectedGenres: List<String>,
        selectedTags: List<String>
    ) {
        _search.value = query

        Timber.d("Updating search with tags $selectedTags")

        mediaSearchState.value = mediaSearchState.value.copy(
            query = query,
            searchType = searchType,
            sort = mediaSort,
            currentSeason = season,
            status = status,
            year = year,
            genres = selectedGenres,
            tags = selectedTags
        )
    }

    fun setMediaSearchType(searchType: SearchFilter) {
        _searchType.value = searchType
        val query = search.value
        when (searchType) {
            SearchFilter.MEDIA, SearchFilter.ANIME, SearchFilter.MANGA -> {
                _mediaSearch.value = query
            }

            SearchFilter.CHARACTERS -> {
                _characterSearch.value = query
            }

            SearchFilter.STAFF -> {
                _staffSearch.value = query
            }

            SearchFilter.STUDIOS -> {
                _studioSearch.value = query
            }

            SearchFilter.THREADS -> {
                _threadSearch.value = query
            }

            SearchFilter.USER -> {
                _userSearch.value = query
            }
        }
    }

    fun setMediaSortType(type: AniMediaSort) {
        _mediaSortType.value = type
    }

    fun setCharacterSortType(type: AniCharacterSort) {
        _characterSortType.value = type
    }

    init {
        viewModelScope.launch {
            when (val tagsData = homeRepository.getTags()) {
                is AniResult.Success -> {
                    _tags.value = tagsData.data;
                }

                is AniResult.Failure -> {
                    //todo handle failure (e.g. toast)
                }
            }
            when (val genreData = homeRepository.getGenres()) {
                is AniResult.Success -> {
                    _genres.value = genreData.data
                }

                is AniResult.Failure -> {
                    //todo handle failure (e.g. toast)
                }
            }

            search.collectLatest { query ->
                Timber.i("Current search filter in init block view model is " + searchType.value)
                when (searchType.value) {
                    SearchFilter.MEDIA, SearchFilter.ANIME, SearchFilter.MANGA -> {
                        _mediaSearch.emit(query)
                    }

                    SearchFilter.CHARACTERS -> {
                        _characterSearch.emit(query)
                    }

                    SearchFilter.STAFF -> {
                        _staffSearch.emit(query)
                    }

                    SearchFilter.STUDIOS -> {
                        _studioSearch.emit(query)
                    }

                    SearchFilter.THREADS -> {
                        _threadSearch.emit(query)
                    }

                    SearchFilter.USER -> {
                        _userSearch.emit(query)
                    }
                }
            }
        }
//        viewModelScope.launch {
//            searchType.collectLatest { searchType ->
//                Log.i(TAG, "New search filter is ${searchType} and query is ${search.value}")
//                if (searchType == SearchFilter.MEDIA || searchType == SearchFilter.ANIME || searchType == SearchFilter.MANGA) {
//                    _mediaSearch.emit(search.value)
//                } else if (searchType == SearchFilter.CHARACTERS) {
//                    _characterSearch.emit(search.value)
//                }
//            }
//        }
    }

//    private fun parseNotification(data: GetNotificationsQuery.Data?): Flow<List<Notification>> {
//        val list = listOf(
//            Notification(
//                type = data?.Page?.notifications?.get(0)?.__typename ?: ""
//            )
//        )
//        return list.asFlow()
//    }

    val notifications = notificationRepository.getNotifications().asLiveData()
//    val notifications: LiveData<List<Notification>> get() = _notifications
//    fun fetchNotifications() {
//        viewModelScope.launch {
//            val data = Apollo.executeQuery(Apollo.apolloClient.query(GetNotificationsQuery()))
//
//            if (data.status == ResultStatus.SUCCESSFUL) {
//                _notifications.value = data.data.
//            }
//        }
//    }

//    fun setAccessCode(accessCode: String) {
//        viewModelScope.launch {
//            userPreferencesRepository.setAccessCode(accessCode)
//        }
//    }

    private val _media = MutableLiveData<HomeMedia>()
    val media: LiveData<HomeMedia> = _media
    private val _popularAnime = MutableLiveData<List<Media>>()
    private val _trendingAnime = MutableLiveData<List<Media>>()
    private val _upcomingNextSeason = MutableLiveData<List<Media>>()
    private val _allTimePopular = MutableLiveData<List<Media>>()
    private val _top100 = MutableLiveData<List<Media>>()

    init {
        fetchMedia(
            isAnime = true,
            page = 1,
            skipPopularThisSeason = false,
            skipTrendingNow = false,
            skipUpcomingNextSeason = false,
            skipAllTimePopular = false,
            skipTop100Anime = false,
        )
    }

    // fixme remove 25 default
    private fun fetchMedia(
        isAnime: Boolean,
        page: Int,
        pageSize: Int = 25,
        skipPopularThisSeason: Boolean = true,
        skipTrendingNow: Boolean = true,
        skipUpcomingNextSeason: Boolean = true,
        skipAllTimePopular: Boolean = true,
        skipTop100Anime: Boolean = true,
    ) {
        viewModelScope.launch {
            val newMedia = homeRepository.getHomeMedia(
                isAnime,
                page,
                pageSize,
                skipTrendingNow,
                skipPopularThisSeason,
                skipUpcomingNextSeason,
                skipAllTimePopular,
                skipTop100Anime,
            ).getOrDefault(HomeMedia())
            if (!skipPopularThisSeason) _popularAnime.value =
                _media.value?.popularThisSeason.orEmpty() + newMedia.popularThisSeason
            if (!skipTrendingNow) _trendingAnime.value =
                _media.value?.trendingNow.orEmpty() + newMedia.trendingNow
            if (!skipUpcomingNextSeason) _upcomingNextSeason.value =
                _media.value?.upcomingNextSeason.orEmpty() + newMedia.upcomingNextSeason
            if (!skipAllTimePopular) _allTimePopular.value =
                _media.value?.allTimePopular.orEmpty() + newMedia.allTimePopular
            if (!skipTop100Anime) _top100.value =
                _media.value?.top100Anime.orEmpty() + newMedia.top100Anime
        }
    }


    fun markAllNotificationsAsRead() {
        // todo
    }
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val myMedia: HomeUiStateData) : HomeUiState
    data class Error(val message: String) : HomeUiState
}