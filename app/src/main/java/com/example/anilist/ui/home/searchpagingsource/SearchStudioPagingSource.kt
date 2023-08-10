package com.example.anilist.ui.home.searchpagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.AniStudio
import com.example.anilist.data.repository.HomeRepository
import timber.log.Timber

private const val STARTING_KEY = 1

class SearchStudioPagingSource(
    private val homeRepository: HomeRepository,
    private val search: String
): PagingSource<Int, AniStudio>() {
    override fun getRefreshKey(state: PagingState<Int, AniStudio>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchor position is the last index that successfully fetched data
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AniStudio> {
        Timber.d("Studio is querying for $search")
        val start = params.key ?: STARTING_KEY
        val data = homeRepository.searchStudio(page = start, pageSize = params.loadSize, text = search)
        return LoadResult.Page(
            data = data,
            prevKey = if (start == STARTING_KEY) null else start - 1,
            nextKey = if (data.isNotEmpty()) start + 1 else null
        )
    }
}