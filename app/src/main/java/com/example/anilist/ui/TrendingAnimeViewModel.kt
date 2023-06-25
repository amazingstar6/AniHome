package com.example.anilist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.anilist.GetTrendsQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrendingAnimeViewModel : ViewModel() {

    private val apolloClient =
        ApolloClient.Builder().serverUrl("https://graphql.anilist.co").build()
    private val _uiState = MutableStateFlow(TrendingAnimeUiState())

    init {
        loadTrendingAnime()
    }

    private var page = 1

    val uiState: StateFlow<TrendingAnimeUiState> = _uiState.asStateFlow()

    private fun loadTrendingAnime() {
        viewModelScope.launch {
            val response =
                apolloClient.query(GetTrendsQuery(Optional.Present(_uiState.value.page))).execute()
            extractResponse(response)
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
                            )
                        )
                    )
                        .execute()
                extractResponse(response)
            }
        }
    }

    private fun extractResponse(response: ApolloResponse<GetTrendsQuery.Data>) {
        _uiState.update { currentState ->
            currentState.copy(
                names = response.data?.Page?.media?.filterNotNull() ?: emptyList()
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