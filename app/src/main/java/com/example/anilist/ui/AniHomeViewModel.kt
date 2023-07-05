package com.example.anilist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.anilist.GetTrendsQuery
import com.example.anilist.type.MediaSeason
import com.example.anilist.type.MediaSort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

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

    private var page = 1

    val uiState: StateFlow<AniHomeUiState> = _uiState.asStateFlow()

    private fun loadTrendingAnime() {
        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetTrendsQuery(
                        Optional.Present(_uiState.value.page),
                        Optional.Present(
                            listOf(MediaSort.TRENDING_DESC)
                        )
                    )
                ).execute()
            extractResponse(response)
        }
    }

    private fun loadPopularAnime() {
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
                        Optional.Present(_uiState.value.page),
                        Optional.Present(listOf( MediaSort.POPULARITY_DESC)),
                        Optional.Present(season),
                        Optional.Present(year)
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    popularAnime = response.data?.trending?.media?.filterNotNull() ?: emptyList()
                )
            }
        }
    }

    private fun loadUpcomingNextSeason() {
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
                        Optional.Present(_uiState.value.page),
                        Optional.Present(listOf( MediaSort.POPULARITY_DESC)),
                        Optional.Present(season),
                        Optional.Present(year)
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    upcomingNextSeason = response.data?.trending?.media?.filterNotNull() ?: emptyList()
                )
            }
        }
    }

    private fun loadAllTimePopular() {
        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetTrendsQuery(
                        Optional.Present(_uiState.value.page),
                        Optional.Present(listOf( MediaSort.POPULARITY_DESC)),
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    allTimePopular = response.data?.trending?.media?.filterNotNull() ?: emptyList()
                )
            }
        }
    }

    fun goToNextPage() {
        if (_uiState.value.page < Int.MAX_VALUE) {
            _uiState.update { currenState ->
                currenState.copy(page = currenState.page.inc())
            }
            viewModelScope.launch {
                val response =
                    apolloClient.query(
                        GetTrendsQuery(
                            Optional.Present(
                                _uiState.value.page
                            ),
                            Optional.Present(listOf(MediaSort.TRENDING_DESC))
                        )
                    )
                        .execute()
                extractResponse(response)
            }
        }
    }

    private fun loadTop100Anime() {
        viewModelScope.launch {
            val response =
                apolloClient.query(
                    GetTrendsQuery(
                        Optional.Present(_uiState.value.page),
                        Optional.Present(listOf( MediaSort.SCORE_DESC)),
                    )
                ).execute()
            _uiState.update { currentState ->
                currentState.copy(
                    top100Anime = response.data?.trending?.media?.filterNotNull() ?: emptyList()
                )
            }
        }
    }

    private fun extractResponse(response: ApolloResponse<GetTrendsQuery.Data>) {
        _uiState.update { currentState ->
            currentState.copy(
                trendingAnime = response.data?.trending?.media?.filterNotNull() ?: emptyList()
            )
        }
    }

    fun goToPreviousPage() {
        if (_uiState.value.page > 1) {
            _uiState.update { currentState ->
                currentState.copy(page = currentState.page.dec())
            }
            viewModelScope.launch {
                val response =
                    apolloClient.query(GetTrendsQuery(Optional.Present(_uiState.value.page.dec())))
                        .execute()
                extractResponse(response)
            }
        }
    }
}