package com.example.anilist.ui.home

import androidx.compose.runtime.State
import androidx.paging.compose.LazyPagingItems
import com.example.anilist.data.models.AniStudio
import com.example.anilist.data.models.AniThread
import com.example.anilist.data.models.AniUser
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StaffDetail

data class HomeUiState(
    val pagerTrendingNow: LazyPagingItems<Media>,
    val pagerPopularThisSeason: LazyPagingItems<Media>,
    val pagerUpcomingNextSeason: LazyPagingItems<Media>,
    val pagerAllTimePopular: LazyPagingItems<Media>,
    val pagerTop100Anime: LazyPagingItems<Media>,
    val searchResultsMedia: LazyPagingItems<Media>,
    val searchResultsCharacter: LazyPagingItems<CharacterDetail>,
    val searchResultsStaff: LazyPagingItems<StaffDetail>,
    val searchResultsStudio: LazyPagingItems<AniStudio>,
    val searchResultsThread: LazyPagingItems<AniThread>,
    val searchResultsUser: LazyPagingItems<AniUser>,
    val searchIsActive: Boolean,
)