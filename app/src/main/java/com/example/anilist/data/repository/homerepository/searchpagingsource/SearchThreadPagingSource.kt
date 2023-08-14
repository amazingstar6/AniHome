package com.example.anilist.data.repository.homerepository.searchpagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniThread
import com.example.anilist.data.repository.homerepository.HomeRepositoryImpl
import timber.log.Timber

private const val STARTING_KEY = 1

class SearchThreadPagingSource(
    private val homeRepository: HomeRepositoryImpl,
    private val search: String
) :
    PagingSource<Int, AniThread>() {

    override fun getRefreshKey(state: PagingState<Int, AniThread>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchor position is the last index that successfully fetched data
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AniThread> {
        Timber.d("Thread is querying for $search")
        val start = params.key ?: STARTING_KEY
        val data =
            homeRepository.searchForum(page = start, pageSize = params.loadSize, text = search)
        return when (data) {
            is AniResult.Failure -> {
                LoadResult.Error(Exception(data.error))
            }

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