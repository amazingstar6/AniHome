package com.example.anilist.ui.home

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.repository.HomeRepository

private const val STARTING_KEY = 1

class SearchCharactersPagingSource(
    private val homeRepository: HomeRepository,
    private val search: String
) : PagingSource<Int, CharacterDetail>() {
    override fun getRefreshKey(state: PagingState<Int, CharacterDetail>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchor position is the last index that successfully fetched data
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CharacterDetail> {
        val start = params.key ?: STARTING_KEY
        val data = homeRepository.searchCharacters(start, params.loadSize, search)
        return LoadResult.Page(
            data = data,
            prevKey = if (start == STARTING_KEY) null else start - 1,
            nextKey = if (data.isNotEmpty()) start + 1 else null
        )
    }

}