package com.example.anilist.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.example.anilist.GetTrendsQuery
import com.example.anilist.data.models.Media
import com.example.anilist.data.repository.HomeMedia
import com.example.anilist.data.repository.HomeRepository
import com.example.anilist.data.repository.NotificationRepository
import com.example.anilist.data.repository.UserPreferencesRepository
import com.example.anilist.data.repository.UserSettings
import com.example.anilist.type.MediaSeason
import com.example.anilist.type.MediaSort
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "AniHomeViewModel"

@HiltViewModel
class AniHomeViewModel @Inject constructor(
    notificationRepository: NotificationRepository,
    homeRepository: HomeRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) :
    ViewModel() {

    val initialSetupEvent = liveData {
        emit(userPreferencesRepository.fetchInitialPreferences())
    }

    // Keep the user preferences as a stream of changes
    private val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow
    private val _userSettings: MutableLiveData<UserSettings> = MutableLiveData()

    //    fun getNotifications:
    val userSettings: LiveData<UserSettings>
        get() = _userSettings

    init {
        _userSettings.value = userPreferencesFlow.asLiveData().value
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

    fun setAccessCode(accessCode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setAccessCode(accessCode)
        }
    }

    private val apolloClient =
        ApolloClient.Builder().serverUrl("https://graphql.anilist.co").build()
    private val _uiState = MutableStateFlow(AniHomeUiState())
    val uiState: StateFlow<AniHomeUiState> = _uiState.asStateFlow()

    private val _media = MutableLiveData<HomeMedia>()
    val media = _media

    init {
//        loadTrendingAnime()
//        loadPopularAnime()
//        loadUpcomingNextSeason()
//        loadAllTimePopular()
//        loadTop100Anime()
        viewModelScope.launch {
            _media.value = homeRepository.getHomeMedia(true).getOrDefault(HomeMedia())
        }
    }

    fun loadTrendingAnime(increasePage: Boolean = false) {
        if (increasePage) {
            _uiState.update { currentState ->
                currentState.copy(trendingPage = currentState.trendingPage.inc())
            }
        }
        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetTrendsQuery(
                        Optional.Present(_uiState.value.trendingPage),
                        Optional.Present(
                            listOf(MediaSort.TRENDING_DESC)
                        )
                    )
                ).execute()
            val data = response.data?.trending?.media
//            if (data == null) Log.i(TAG, "Trending data list is empty!")
            _uiState.update { currentState ->
                currentState.copy(
                    trendingAnime = currentState.trendingAnime + parseMediaHome(
                        data?.filterNotNull()
                            .orEmpty()
                    )
                )
            }
        }
    }

    private fun parseMediaHome(medias: List<GetTrendsQuery.Medium>): List<Media> {
        val mediaList: MutableList<Media> = mutableListOf()
        for (media in medias) {
            if (media.title?.userPreferred != null && media.coverImage?.extraLarge != null) {
                mediaList.add(
                    Media(
                        id = media.id,
                        title = media.title.userPreferred,
                        coverImage = media.coverImage.extraLarge,
                        note = ""
                    )
                )
            }
        }
        return mediaList
    }

    fun loadPopularAnime(increasePage: Boolean = false) {
        if (increasePage) {
            _uiState.update { currentState ->
                currentState.copy(popularPage = currentState.popularPage.inc())
            }
        }
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val season: MediaSeason
        when (month) {
            Calendar.JANUARY -> season = MediaSeason.WINTER
            Calendar.FEBRUARY -> season = MediaSeason.WINTER
            Calendar.MARCH -> season = MediaSeason.WINTER

            Calendar.APRIL -> season = MediaSeason.SPRING
            Calendar.MAY -> season = MediaSeason.SPRING
            Calendar.JUNE -> season = MediaSeason.SPRING

            Calendar.JULY -> season = MediaSeason.SUMMER
            Calendar.AUGUST -> season = MediaSeason.SUMMER
            Calendar.SEPTEMBER -> season = MediaSeason.SUMMER

            Calendar.OCTOBER -> season = MediaSeason.FALL
            Calendar.NOVEMBER -> season = MediaSeason.FALL
            Calendar.DECEMBER -> season = MediaSeason.FALL

            else -> season = MediaSeason.UNKNOWN__
        }

        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetTrendsQuery(
                        Optional.Present(_uiState.value.popularPage),
                        Optional.Present(listOf(MediaSort.POPULARITY_DESC)),
                        Optional.Present(season),
                        Optional.Present(year)
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    popularAnime = currentState.popularAnime + parseMediaHome(
                        response.data?.trending?.media?.filterNotNull()
                            .orEmpty()
                    )
                )
            }
        }
    }

    fun loadUpcomingNextSeason(increasePage: Boolean = false) {
        if (increasePage) {
            _uiState.update { currentState ->
                currentState.copy(
                    upcomingNextSeasonPage = currentState.upcomingNextSeasonPage.inc()
                )
            }
        }
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val season: MediaSeason
        // using the next season, since we're loading the upcoming season
        when (month) {
            Calendar.JANUARY -> season = MediaSeason.SPRING
            Calendar.FEBRUARY -> season = MediaSeason.SPRING
            Calendar.MARCH -> season = MediaSeason.SPRING

            Calendar.APRIL -> season = MediaSeason.SUMMER
            Calendar.MAY -> season = MediaSeason.SUMMER
            Calendar.JUNE -> season = MediaSeason.SUMMER

            Calendar.JULY -> season = MediaSeason.FALL
            Calendar.AUGUST -> season = MediaSeason.FALL
            Calendar.SEPTEMBER -> season = MediaSeason.FALL

            Calendar.OCTOBER -> season = MediaSeason.WINTER
            Calendar.NOVEMBER -> season = MediaSeason.WINTER
            Calendar.DECEMBER -> season = MediaSeason.WINTER

            else -> season = MediaSeason.UNKNOWN__
        }
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val year = if (season == MediaSeason.WINTER) currentYear + 1 else currentYear

        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetTrendsQuery(
                        Optional.Present(_uiState.value.upcomingNextSeasonPage),
                        Optional.Present(listOf(MediaSort.POPULARITY_DESC)),
                        Optional.Present(season),
                        Optional.Present(year)
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    upcomingNextSeason = currentState.upcomingNextSeason + parseMediaHome(
                        response.data?.trending?.media?.filterNotNull()
                            .orEmpty()
                    )
                )
            }
        }
    }

    fun loadAllTimePopular(increasePage: Boolean = true) {
        if (increasePage) {
            _uiState.update { currentState ->
                currentState.copy(allTimePopularPage = currentState.allTimePopularPage.inc())
            }
        }
        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetTrendsQuery(
                        Optional.Present(_uiState.value.allTimePopularPage),
                        Optional.Present(listOf(MediaSort.POPULARITY_DESC))
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    allTimePopular = currentState.allTimePopular + parseMediaHome(
                        response.data?.trending?.media?.filterNotNull()
                            .orEmpty()
                    )
                )
            }
        }
    }

    fun loadTop100Anime(increasePage: Boolean = false) {
        if (increasePage) {
            _uiState.update { currentState ->
                currentState.copy(top100AnimePage = currentState.top100AnimePage.inc())
            }
        }
        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetTrendsQuery(
                        Optional.Present(_uiState.value.top100AnimePage),
                        Optional.Present(listOf(MediaSort.SCORE_DESC))
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    top100Anime = currentState.top100Anime + parseMediaHome(
                        response.data?.trending?.media?.filterNotNull()
                            .orEmpty()
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "AniHomeViewModel was just cleared!")
    }

    fun markAllNotificationsAsRead() {
        // todo
    }

//    fun saveLoginDetails(userSettings: UserSettings) {
//        viewModelScope.launch {
//            dataStoreManager.saveToDataStore(userSettings)
//        }
//    }
//
//    fun getFromDataStore() {
//        viewModelScope.launch {
//            dataStoreManager.getFromDataStore().first()
//        }
//    }
}
