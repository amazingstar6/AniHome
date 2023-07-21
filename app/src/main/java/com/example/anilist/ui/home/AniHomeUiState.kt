package com.example.anilist.ui.home

import com.example.anilist.data.models.Character
import com.example.anilist.data.models.Media

data class AniHomeUiState(
    val trendingAnime: List<Media> = emptyList(),
    val popularAnime: List<Media> = emptyList(),
    val upcomingNextSeason: List<Media> = emptyList(),
    val allTimePopular: List<Media> = emptyList(),
    val top100Anime: List<Media> = emptyList(),
    var trendingPage: Int = 1,
    var popularPage: Int = 1,
    var upcomingNextSeasonPage: Int = 1,
    var allTimePopularPage: Int = 1,
    var top100AnimePage: Int = 1,
    var currentDetailAnime: Media = Media(note = ""),
    var currentDetailCharacters: List<Character> = emptyList(),
    val personalAnimeList: List<Media> = emptyList(),
    val isLoggedIn: Boolean = false,
    val accessCode: String = ""
)
