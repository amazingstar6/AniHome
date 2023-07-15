package com.example.anilist.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.example.anilist.GetAnimeInfoQuery
import com.example.anilist.GetTrendsQuery
import com.example.anilist.type.MediaSeason
import com.example.anilist.type.MediaSort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

private const val TAG = "AniHomeViewModel"

class AniHomeViewModel : ViewModel() {

    private val apolloClient =
        ApolloClient.Builder().serverUrl("https://graphql.anilist.co").build()
    private val _uiState = MutableStateFlow(AniHomeUiState())
    init {
        loadTrendingAnime()
        loadPopularAnime()
        loadUpcomingNextSeason()
        loadAllTimePopular()
        loadTop100Anime()
    }

    val uiState: StateFlow<AniHomeUiState> = _uiState.asStateFlow()

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
                    trendingAnime = currentState.trendingAnime + data?.filterNotNull()
                        .orEmpty()
                )
            }
        }
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
                    popularAnime = currentState.popularAnime + response.data?.trending?.media?.filterNotNull()
                        .orEmpty()
                )
            }
        }
    }

    fun loadUpcomingNextSeason(increasePage: Boolean = false) {
        if (increasePage) {
            _uiState.update { currentState ->
                currentState.copy(upcomingNextSeasonPage = currentState.upcomingNextSeasonPage.inc())
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
                    upcomingNextSeason = currentState.upcomingNextSeason + response.data?.trending?.media?.filterNotNull()
                        .orEmpty()
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
                        Optional.Present(listOf(MediaSort.POPULARITY_DESC)),
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    allTimePopular = currentState.allTimePopular + response.data?.trending?.media?.filterNotNull()
                        .orEmpty()
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
                        Optional.Present(listOf(MediaSort.SCORE_DESC)),
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    top100Anime = currentState.top100Anime + response.data?.trending?.media?.filterNotNull()
                        .orEmpty()
                )
            }
        }
    }

    fun getAnimeDetails(id: Int) {
        viewModelScope.launch {
            val response =
                apolloClient.query(GetAnimeInfoQuery(id)).execute()
            _uiState.update { currentState ->
                currentState.copy(currentDetailAnime = response.data?.Media)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "AniHomeViewModel was just cleared!")
    }
}