package com.example.anilist.ui.home.searchpagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.AniMediaStatus
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.Season
import com.example.anilist.data.repository.HomeRepository
import com.example.anilist.ui.home.AniMediaSort
import com.example.anilist.ui.home.SearchFilter
import timber.log.Timber

private const val STARTING_KEY = 1
private const val TAG = "SearchPagingSource"

class SearchMediaPagingSource(
    private val homeRepository: HomeRepository,
    private val search: String,
    private val mediaSearchType: SearchFilter,
    private val sortType: AniMediaSort,
    private val season: Season,
    private val status: AniMediaStatus,
    private val year: Int
) : PagingSource<Int, Media>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Media> {
        val start = params.key ?: STARTING_KEY
        val data =
            homeRepository.searchMedia(
                page = start,
                pageSize = params.loadSize,
                text = search,
                type = mediaSearchType,
                sort = sortType,
                season = season,
                status = status,
                year = year
            )
        Timber.d("Current search page being loaded is " + params.key + " with query " + search)
        return when (data) {
            is AniResult.Failure -> LoadResult.Error(Exception(data.error))
            is AniResult.Success -> LoadResult.Page(
                data = data.data,
                prevKey = if (start == STARTING_KEY) null else start - 1,
                nextKey = if (data.data.isNotEmpty()) start + 1 else null
            )
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Media>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchor position is the last index that successfully fetched data
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

}