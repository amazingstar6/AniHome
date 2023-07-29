package com.example.anilist.ui.home

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.Media
import com.example.anilist.data.repository.HomeRepository

private const val STARTING_KEY = 1
private const val TAG = "SearchPagingSource"

class SearchPagingSource(
    private val homeRepository: HomeRepository,
    private val search: String,
    private val mediaSearchType: SearchFilter,
    private val sortType: AniMediaSort
) : PagingSource<Int, Media>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Media> {
        val start = params.key ?: STARTING_KEY
        val data = homeRepository.searchMedia(start, params.loadSize, search, mediaSearchType, sortType)
        Log.d(TAG, "Data received in search paging source is $data")
        Log.d(TAG, "Current search page being loaded is ${params.key} with query $search")
        return LoadResult.Page(
            data = data,
            prevKey = if (start == STARTING_KEY) null else start - 1,
            nextKey = if (data.isNotEmpty()) start + 1 else null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Media>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchor position is the last index that successfully fetched data
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

}