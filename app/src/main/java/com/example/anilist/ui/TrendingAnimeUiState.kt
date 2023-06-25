package com.example.anilist.ui

import com.example.anilist.GetTrendsQuery

data class TrendingAnimeUiState(
    val names: List<GetTrendsQuery.Medium> = emptyList(),
    var page: Int = 1
)