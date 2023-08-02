package com.example.anilist.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.Media

private const val STARTING_KEY = 1

class StudioMediaPagingSource (
    private val studioDetailRepository: StudioDetailRepository,
    private val studioId: Int
) : PagingSource<Int, Media>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Media> {
        val start = params.key ?: STARTING_KEY
        val data = studioDetailRepository.fetchMedia(studioId, start, params.loadSize)
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