package com.example.anilist.data.repository.mediadetail

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniStaff

private const val STARTING_KEY = 1

class StaffPagingSource(
    private val mediaId: Int,
    private val mediaDetailsRepository: MediaDetailsRepository
) : PagingSource<Int, AniStaff>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AniStaff> {
        val start = params.key ?: STARTING_KEY
        return when (val data = mediaDetailsRepository.fetchStaffList(mediaId, start, params.loadSize)) {
            is AniResult.Success -> {
                LoadResult.Page(
                    data = data.data,
                    prevKey = if (start == STARTING_KEY) null else start - 1,
                    nextKey = if (data.data.isNotEmpty()) start + 1 else null
                )
            }

            is AniResult.Failure -> {
                LoadResult.Error(Exception(data.error))
            }
        }

    }

    override fun getRefreshKey(state: PagingState<Int, AniStaff>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchor position is the last index that successfully fetched data
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }
}