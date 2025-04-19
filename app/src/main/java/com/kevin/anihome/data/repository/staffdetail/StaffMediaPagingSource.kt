package com.kevin.anihome.data.repository.staffdetail

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kevin.anihome.data.models.AniCharacterMediaConnection
import com.kevin.anihome.data.models.AniResult

private const val STARTING_KEY = 1

class StaffMediaPagingSource(
    private val staffDetailRepository: StaffDetailRepository,
    private val staffId: Int,
) : PagingSource<Int, AniCharacterMediaConnection>() {
    override fun getRefreshKey(state: PagingState<Int, AniCharacterMediaConnection>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchor position is the last index that successfully fetched data
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AniCharacterMediaConnection> {
        val start = params.key ?: STARTING_KEY
        return when (val data = staffDetailRepository.fetchStaffMedia(staffId, start, params.loadSize)) {
            is AniResult.Success -> {
                LoadResult.Page(
                    data = data.data,
                    prevKey = if (start == STARTING_KEY) null else start - 1,
                    nextKey = if (data.data.isNotEmpty()) start + 1 else null,
                )
            }

            is AniResult.Failure -> {
                LoadResult.Error(Exception(data.error))
            }
        }
    }
}
