package com.kevin.anihome.data.repository.homerepository

import com.kevin.anihome.data.models.AniCharacterDetail
import com.kevin.anihome.data.models.AniResult
import com.kevin.anihome.data.models.AniStaffDetail
import com.kevin.anihome.data.models.AniStudio
import com.kevin.anihome.data.models.AniTag
import com.kevin.anihome.data.models.AniThread
import com.kevin.anihome.data.models.AniUser
import com.kevin.anihome.data.models.Media
import com.kevin.anihome.ui.home.AniCharacterSort
import com.kevin.anihome.ui.home.MediaSearchState

interface HomeRepository {
    fun trendingNowPagingSource(isAnime: Boolean): MediaPagingSource

    fun popularThisSeasonPagingSource(): MediaPagingSource

    fun upcomingNextSeasonPagingSource(): MediaPagingSource

    fun allTimePopularPagingSource(isAnime: Boolean): MediaPagingSource

    fun top100AnimePagingSource(isAnime: Boolean): MediaPagingSource

    fun popularManhwaPagingSource(): MediaPagingSource

    suspend fun getTrendingNow(
        isAnime: Boolean,
        page: Int,
        pageSize: Int,
    ): AniResult<List<Media>>

    suspend fun getPopularThisSeason(
        page: Int,
        pageSize: Int,
    ): AniResult<List<Media>>

    suspend fun getUpcomingNextSeason(
        page: Int,
        pageSize: Int,
    ): AniResult<List<Media>>

    suspend fun getAllTimePopularMedia(
        isAnime: Boolean,
        page: Int,
        pageSize: Int,
    ): AniResult<List<Media>>

    suspend fun getTop100Anime(
        isAnime: Boolean,
        page: Int,
        pageSize: Int,
    ): AniResult<List<Media>>

    suspend fun getPopularManhwa(
        page: Int,
        pageSize: Int,
    ): AniResult<List<Media>>

    suspend fun searchMedia(
        page: Int,
        pageSize: Int,
        searchState: MediaSearchState,
    ): AniResult<List<Media>>

    suspend fun searchCharacters(
        page: Int,
        pageSize: Int,
        text: String,
        sort: AniCharacterSort,
    ): AniResult<List<AniCharacterDetail>>

    suspend fun searchStaff(
        text: String,
        page: Int,
        pageSize: Int,
    ): AniResult<List<AniStaffDetail>>

    suspend fun searchStudio(
        text: String,
        page: Int,
        pageSize: Int,
    ): AniResult<List<AniStudio>>

    suspend fun searchForum(
        text: String,
        page: Int,
        pageSize: Int,
    ): AniResult<List<AniThread>>

    suspend fun searchUser(
        text: String,
        page: Int,
        pageSize: Int,
    ): AniResult<List<AniUser>>

    suspend fun getTags(): AniResult<List<AniTag>>

    suspend fun getGenres(): AniResult<List<String>>
}
