package com.kevin.anihome.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kevin.anihome.data.models.AniCharacterDetail
import com.kevin.anihome.data.models.AniMediaStatus
import com.kevin.anihome.data.models.AniResult
import com.kevin.anihome.data.models.AniSeason
import com.kevin.anihome.data.models.AniStaffDetail
import com.kevin.anihome.data.models.AniStudio
import com.kevin.anihome.data.models.AniTag
import com.kevin.anihome.data.models.AniThread
import com.kevin.anihome.data.models.AniUser
import com.kevin.anihome.data.models.Media
import com.kevin.anihome.data.repository.homerepository.HomeRepositoryImpl
import com.kevin.anihome.data.repository.homerepository.searchpagingsource.SearchCharactersPagingSource
import com.kevin.anihome.data.repository.homerepository.searchpagingsource.SearchMediaPagingSource
import com.kevin.anihome.data.repository.homerepository.searchpagingsource.SearchStaffPagingSource
import com.kevin.anihome.data.repository.homerepository.searchpagingsource.SearchStudioPagingSource
import com.kevin.anihome.data.repository.homerepository.searchpagingsource.SearchThreadPagingSource
import com.kevin.anihome.data.repository.homerepository.searchpagingsource.SearchUserPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val TIME_OUT = 300L // milli seconds
private const val PAGE_SIZE = 25
const val PREFETCH_DISTANCE = 10

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val homeRepository: HomeRepositoryImpl,
    ) :
    ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState.Loading)
        val uiState: StateFlow<HomeUiState> = _uiState

        private var _isAnime = MutableStateFlow(true)
        val isAnime: StateFlow<Boolean> = _isAnime

        private var _tags: MutableStateFlow<List<AniTag>> = MutableStateFlow(emptyList())
        val tags: StateFlow<List<AniTag>> = _tags

        private var _genres: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
        val genres: StateFlow<List<String>> = _genres

        private val _toastMessage = MutableSharedFlow<String>()
        val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

        private fun sendMessage(message: String) {
            viewModelScope.launch {
                _toastMessage.emit(message)
            }
        }

        fun setToAnime() {
            _isAnime.value = true
        }

        fun setToManga() {
            _isAnime.value = false
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        val trendingNowPager: Flow<PagingData<Media>> =
            isAnime.flatMapLatest {
                Pager(
                    config =
                        PagingConfig(
                            pageSize = PAGE_SIZE,
                            prefetchDistance = PREFETCH_DISTANCE,
                            enablePlaceholders = true,
                        ),
                    pagingSourceFactory = {
                        homeRepository.trendingNowPagingSource(isAnime = isAnime.value)
                    },
                ).flow.cachedIn(viewModelScope)
            }

        val popularThisSeasonPager =
            Pager(
                config =
                    PagingConfig(
                        pageSize = PAGE_SIZE,
                        prefetchDistance = PREFETCH_DISTANCE,
                        enablePlaceholders = false,
                    ),
                pagingSourceFactory = { homeRepository.popularThisSeasonPagingSource() },
            ).flow.cachedIn(viewModelScope)

        val upComingNextSeasonPager =
            Pager(
                config =
                    PagingConfig(
                        pageSize = PAGE_SIZE,
                        prefetchDistance = PREFETCH_DISTANCE,
                        enablePlaceholders = false,
                    ),
                pagingSourceFactory = { homeRepository.upcomingNextSeasonPagingSource() },
            ).flow.cachedIn(viewModelScope)

        @OptIn(ExperimentalCoroutinesApi::class)
        val allTimePopularPager =
            isAnime.flatMapLatest {
                Pager(
                    config =
                        PagingConfig(
                            pageSize = 25,
                            prefetchDistance = PREFETCH_DISTANCE,
                            enablePlaceholders = false,
                        ),
                    pagingSourceFactory = {
                        homeRepository.allTimePopularPagingSource(isAnime = isAnime.value)
                    },
                ).flow.cachedIn(viewModelScope)
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        val top100AnimePager =
            isAnime.flatMapLatest {
                Pager(
                    config =
                        PagingConfig(
                            pageSize = PAGE_SIZE,
                            prefetchDistance = PREFETCH_DISTANCE,
                            enablePlaceholders = false,
                        ),
                    pagingSourceFactory = {
                        homeRepository.top100AnimePagingSource(isAnime = isAnime.value)
                    },
                ).flow.cachedIn(viewModelScope)
            }

        val popularManhwaPager =
            Pager(
                config =
                    PagingConfig(
                        pageSize = PAGE_SIZE,
                        prefetchDistance = PREFETCH_DISTANCE,
                        enablePlaceholders = false,
                    ),
                pagingSourceFactory = { homeRepository.popularManhwaPagingSource() },
            ).flow.cachedIn(viewModelScope)

        private val _search = MutableStateFlow("")
        val search =
            _search.asStateFlow()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(),
                    initialValue = "",
                )

        // fixme do we use this?
        private val _searchType = MutableStateFlow(SearchFilter.MEDIA)
        val searchType =
            _searchType.asStateFlow().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                initialValue = SearchFilter.MEDIA,
            )

        private val mediaSearchState =
            MutableStateFlow(
                MediaSearchState(
                    query = "",
                    searchType = SearchFilter.MEDIA,
                    mediaSort = AniMediaSort.POPULARITY,
                    currentSeason = AniSeason.UNKNOWN,
                    status = AniMediaStatus.UNKNOWN,
                    year = -1,
                    genres = emptyList(),
                    tags = emptyList(),
                    characterSort = AniCharacterSort.FAVOURITES_DESC,
                    onlyOnMyList = false,
                ),
            )

        @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
        val searchResultsMedia =
            mediaSearchState.debounce(TIME_OUT).flatMapLatest { searchState ->
                if (searchState.searchType == SearchFilter.MEDIA ||
                    searchState.searchType == SearchFilter.ANIME ||
                    searchState.searchType == SearchFilter.MANGA
                ) {
                    Timber.d("Media search is searching for " + searchState.query)
                    Pager(
                        config =
                            PagingConfig(
                                pageSize = PAGE_SIZE,
                                prefetchDistance = 5,
                                enablePlaceholders = true,
                            ),
                        pagingSourceFactory = {
                            SearchMediaPagingSource(
                                homeRepository = homeRepository,
                                searchState = searchState,
                            )
                        },
                    ).flow.cachedIn(viewModelScope)
                } else {
                    emptyFlow()
                }
            }

        @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
        val searchResultsCharacter: Flow<PagingData<AniCharacterDetail>> =
            mediaSearchState
                .debounce(300).flatMapLatest { searchState ->
                    if (searchState.searchType == SearchFilter.CHARACTERS) {
                        Timber.d(
                            "Character search is searching for " + searchState.query + " with sort " + searchState.mediaSort,
                        )
                        Pager(
                            config =
                                PagingConfig(
                                    pageSize = PAGE_SIZE,
                                    prefetchDistance = 5,
                                    enablePlaceholders = true,
                                ),
                            pagingSourceFactory = {
                                SearchCharactersPagingSource(
                                    homeRepository = homeRepository,
                                    search = searchState.query,
                                    sortType = searchState.characterSort,
                                )
                            },
                        ).flow.cachedIn(viewModelScope)
                    } else {
                        emptyFlow()
                    }
                }

        @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
        val searchResultsStaff: Flow<PagingData<AniStaffDetail>> =
            mediaSearchState.debounce(300).flatMapLatest { state ->
                if (state.searchType == SearchFilter.STAFF) {
                    Pager(
                        config =
                            PagingConfig(
                                pageSize = PAGE_SIZE,
                                prefetchDistance = 5,
                                enablePlaceholders = true,
                            ),
                        pagingSourceFactory = {
                            SearchStaffPagingSource(
                                homeRepository = homeRepository,
                                search = state.query,
                            )
                        },
                    ).flow.cachedIn(viewModelScope)
                } else {
                    emptyFlow()
                }
            }

        @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
        val searchResultsStudio: Flow<PagingData<AniStudio>> =
            mediaSearchState.debounce(
                TIME_OUT,
            ).flatMapLatest { state ->
                if (state.searchType == SearchFilter.STUDIOS) {
                    Pager(
                        config =
                            PagingConfig(
                                pageSize = PAGE_SIZE,
                                prefetchDistance = 5,
                                enablePlaceholders = true,
                            ),
                        pagingSourceFactory = {
                            SearchStudioPagingSource(
                                homeRepository = homeRepository,
                                search = state.query,
                            )
                        },
                    ).flow.cachedIn(viewModelScope)
                } else {
                    emptyFlow()
                }
            }

        @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
        val searchResultsThread: Flow<PagingData<AniThread>> =
            mediaSearchState.debounce(TIME_OUT).flatMapLatest { state ->
                if (state.searchType == SearchFilter.THREADS) {
                    Pager(
                        config =
                            PagingConfig(
                                pageSize = PAGE_SIZE,
                                prefetchDistance = 5,
                                enablePlaceholders = true,
                            ),
                        pagingSourceFactory = {
                            SearchThreadPagingSource(
                                homeRepository = homeRepository,
                                search = state.query,
                            )
                        },
                    ).flow.cachedIn(viewModelScope)
                } else {
                    emptyFlow()
                }
            }

        @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
        val searchResultsUser: Flow<PagingData<AniUser>> =
            mediaSearchState.debounce(TIME_OUT).flatMapLatest { state ->
                if (state.searchType == SearchFilter.USER) {
                    Pager(
                        config =
                            PagingConfig(
                                pageSize = PAGE_SIZE,
                                prefetchDistance = 5,
                                enablePlaceholders = true,
                            ),
                        pagingSourceFactory = {
                            SearchUserPagingSource(
                                homeRepository = homeRepository,
                                search = state.query,
                            )
                        },
                    ).flow.cachedIn(viewModelScope)
                } else {
                    emptyFlow()
                }
            }

        fun setSearch(searchState: MediaSearchState) {
            _search.value = searchState.query

            Timber.d("Updating search with search type $searchType")

            mediaSearchState.value = searchState
        }

        fun setMediaSearchType(searchType: SearchFilter) {
            mediaSearchState.value = mediaSearchState.value.copy(searchType = searchType)

            _searchType.value = searchType
        }

        init {
            fetchTags()
            fetchGenres()
        }

        fun fetchGenres() {
            viewModelScope.launch {
                when (val genreData = homeRepository.getGenres()) {
                    is AniResult.Success -> {
                        _genres.value = genreData.data
                    }

                    is AniResult.Failure -> {
                        sendMessage("Failed to load genres, please refresh")
                    }
                }
            }
        }

        fun fetchTags() {
            viewModelScope.launch {
                when (val tagsData = homeRepository.getTags()) {
                    is AniResult.Success -> {
                        _tags.value = tagsData.data
                    }

                    is AniResult.Failure -> {
                        sendMessage("Failed to load tags, please refresh")
                    }
                }
            }
        }
    }

data class MediaSearchState(
    val query: String,
    val searchType: SearchFilter,
    val mediaSort: AniMediaSort,
    val characterSort: AniCharacterSort,
    val currentSeason: AniSeason,
    val status: AniMediaStatus,
    val year: Int,
    val genres: List<String>,
    val tags: List<String>,
    val onlyOnMyList: Boolean,
)

// fixme not being used
sealed interface HomeUiState {
    object Loading : HomeUiState

    data class Success(val myMedia: HomeUiStateData) : HomeUiState

    data class Error(val message: String) : HomeUiState
}
