package com.example.anilist.data.animeDetails

import com.example.anilist.GetTrendsQuery

data class AnimeList(
    val trendingAnime: List<GetTrendsQuery.Medium> = emptyList(),
    val popularAnime: List<GetTrendsQuery.Medium> = emptyList(),
    var page: Int = 1
)

class AnimeListRepository {


}