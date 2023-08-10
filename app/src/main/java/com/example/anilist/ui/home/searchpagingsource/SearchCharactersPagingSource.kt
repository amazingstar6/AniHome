package com.example.anilist.ui.home.searchpagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.CharacterDetail
import com.example.anilist.data.repository.HomeRepository
import com.example.anilist.ui.home.AniCharacterSort
import timber.log.Timber

private const val STARTING_KEY = 1

class SearchCharactersPagingSource(
    private val homeRepository: HomeRepository,
    private val search: String,
    private val sortType: AniCharacterSort
) : PagingSource<Int, CharacterDetail>() {
    override fun getRefreshKey(state: PagingState<Int, CharacterDetail>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchor position is the last index that successfully fetched data
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CharacterDetail> {
        Timber.d("Character is querying for $search")
        val start = params.key ?: STARTING_KEY
        return when (val data = homeRepository.searchCharacters(start, params.loadSize, search, sortType)) {
            is AniResult.Failure -> LoadResult.Error(Exception(data.error))
            is AniResult.Success -> {
                LoadResult.Page(
                    data = data.data,
                    prevKey = if (start == STARTING_KEY) null else start - 1,
                    nextKey = if (data.data.isNotEmpty()) start + 1 else null
                )
            }
        }
    }

}