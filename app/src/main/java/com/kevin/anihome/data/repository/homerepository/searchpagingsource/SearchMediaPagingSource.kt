package com.kevin.anihome.data.repository.homerepository.searchpagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kevin.anihome.data.models.AniResult
import com.kevin.anihome.data.models.Media
import com.kevin.anihome.data.repository.homerepository.HomeRepositoryImpl
import com.kevin.anihome.ui.home.MediaSearchState
import timber.log.Timber

private const val STARTING_KEY = 1

class SearchMediaPagingSource(
    private val homeRepository: HomeRepositoryImpl,
    private val searchState: MediaSearchState,
) : PagingSource<Int, Media>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Media> {
        val start = params.key ?: STARTING_KEY
        val data =
            homeRepository.searchMedia(
                page = start,
                pageSize = params.loadSize,
                searchState = searchState,
            )
        Timber.i("Media search is querying ${searchState.query} for ${searchState.searchType}")
        return when (data) {
            is AniResult.Failure -> LoadResult.Error(Exception(data.error))
            is AniResult.Success ->
                LoadResult.Page(
                    data = data.data,
                    prevKey = if (start == STARTING_KEY) null else start - 1,
                    nextKey = if (data.data.isNotEmpty()) start + 1 else null,
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
