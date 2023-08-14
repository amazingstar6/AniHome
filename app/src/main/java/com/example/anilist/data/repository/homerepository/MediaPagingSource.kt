package com.example.anilist.data.repository.homerepository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.HomeTrendingTypes
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.min

private const val STARTING_KEY = 1
private const val PAGE_SIZE = 25
private const val TAG = "MediaPagingSource"

class MediaPagingSource @Inject constructor(
    private val homeRepository: HomeRepositoryImpl,
    private val type: HomeTrendingTypes,
    private val isAnime: Boolean
) : PagingSource<Int, Media>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Media> {
        val start = params.key ?: STARTING_KEY
        Timber.d("Current key in media paging source is " + params.key)
        val pageSize = min(PAGE_SIZE, params.loadSize)
        val data: AniResult<List<Media>> = when (type) {
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
        return when (data) {
            is AniResult.Failure -> {
                LoadResult.Error(Exception(data.error))
            }
            is AniResult.Success -> LoadResult.Page(
                data = data.data,
                prevKey = when (start) {
                    STARTING_KEY -> null
//                else -> ensureValidKey(key = range.first - params.loadSize)
                    else -> start - 1
                },
                nextKey = if (data.data.isNotEmpty()) start + 1 else null
            )
        }
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