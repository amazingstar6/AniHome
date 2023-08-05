package com.example.anilist.ui.home

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.Media
import com.example.anilist.data.repository.HomeRepository
import com.example.anilist.data.repository.HomeTrendingTypes
import com.example.anilist.data.repository.MyMediaRepository
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

private const val STARTING_KEY = 1
private const val PAGE_SIZE = 25
private const val TAG = "MediaPagingSource"

class MediaPagingSource @Inject constructor(
    private val homeRepository: HomeRepository,
    private val type: HomeTrendingTypes,
    private val isAnime: Boolean
) : PagingSource<Int, Media>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Media> {
        val start = params.key ?: STARTING_KEY
//        val range = start.until(start + params.loadSize)
        Log.d(TAG, "Current key in media paging source is ${params.key}")
        val pageSize = min(PAGE_SIZE, params.loadSize)
        val data = when (type) {
            HomeTrendingTypes.TRENDING_NOW -> homeRepository.getTrendingNow(
                isAnime = isAnime,
                page = start,
                pageSize = pageSize
            )

            HomeTrendingTypes.POPULAR_THIS_SEASON -> homeRepository.getPopularThisSeason(
                page = start,
                pageSize = pageSize
            )

            HomeTrendingTypes.UPCOMING_NEXT_SEASON -> homeRepository.getUpcomingNextSeason(
                page = start,
                pageSize = pageSize
            )

            HomeTrendingTypes.ALL_TIME_POPULAR -> homeRepository.getAllTimePopularMedia(
                isAnime = isAnime,
                page = start,
                pageSize = pageSize
            )

            HomeTrendingTypes.TOP_100_ANIME -> homeRepository.getTop100Anime(
                isAnime = isAnime,
                page = start,
                pageSize = pageSize
            )

            HomeTrendingTypes.POPULAR_MANHWA -> homeRepository.getPopularManhwa(
                page = start,
                pageSize = pageSize
            )
        }
        return LoadResult.Page(
            data = data,
            prevKey = when (start) {
                STARTING_KEY -> null
//                else -> ensureValidKey(key = range.first - params.loadSize)
                else -> start - 1
            },
            nextKey = if (data.isNotEmpty()) start + 1 else null
        )
    }

    // The refresh key is used for the initial load of the next PagingSource, after invalidation
    override fun getRefreshKey(state: PagingState<Int, Media>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchor position is the last index that successfully fetched data
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

}