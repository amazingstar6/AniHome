package com.example.anilist.ui

import com.example.anilist.GetTrendsQuery

data class AniHomeUiState(
    val trendingAnime: List<GetTrendsQuery.Medium> = emptyList(),
    val popularAnime: List<GetTrendsQuery.Medium> = emptyList(),
    val upcomingNextSeason: List<GetTrendsQuery.Medium> = emptyList(),
    val allTimePopular: List<GetTrendsQuery.Medium> = emptyList(),
    val top100Anime: List<GetTrendsQuery.Medium> = emptyList(),
    var page: Int = 1
)