package com.example.anilist.ui

import com.example.anilist.data.models.Anime
import com.example.anilist.data.models.Character

data class AniHomeUiState(
    val trendingAnime: List<Anime> = emptyList(),
    val popularAnime: List<Anime> = emptyList(),
    val upcomingNextSeason: List<Anime> = emptyList(),
    val allTimePopular: List<Anime> = emptyList(),
    val top100Anime: List<Anime> = emptyList(),
    var trendingPage: Int = 1,
    var popularPage: Int = 1,
    var upcomingNextSeasonPage: Int = 1,
    var allTimePopularPage: Int = 1,
    var top100AnimePage: Int = 1,
    var currentDetailAnime: Anime = Anime(),
    var currentDetailCharacters: List<Character> = emptyList(),
    val personalAnimeList: List<Anime> = emptyList(),
    val isLoggedIn: Boolean = false,
    val accessCode: String = "",
)