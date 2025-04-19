package com.kevin.anihome.ui.home

import androidx.paging.compose.LazyPagingItems
import com.kevin.anihome.data.models.AniCharacterDetail
import com.kevin.anihome.data.models.AniStaffDetail
import com.kevin.anihome.data.models.AniStudio
import com.kevin.anihome.data.models.AniThread
import com.kevin.anihome.data.models.AniUser
import com.kevin.anihome.data.models.Media

// fixme move this to view model
data class HomeUiStateData(
    val pagerTrendingNow: LazyPagingItems<Media>,
    val pagerPopularThisSeason: LazyPagingItems<Media>,
    val pagerUpcomingNextSeason: LazyPagingItems<Media>,
    val pagerAllTimePopular: LazyPagingItems<Media>,
    val pagerTop100Anime: LazyPagingItems<Media>,
    val pagerPopularManhwa: LazyPagingItems<Media>,
    val searchResultsMedia: LazyPagingItems<Media>,
    val searchResultsCharacter: LazyPagingItems<AniCharacterDetail>,
    val searchResultsStaff: LazyPagingItems<AniStaffDetail>,
    val searchResultsStudio: LazyPagingItems<AniStudio>,
    val searchResultsThread: LazyPagingItems<AniThread>,
    val searchResultsUser: LazyPagingItems<AniUser>,
)
