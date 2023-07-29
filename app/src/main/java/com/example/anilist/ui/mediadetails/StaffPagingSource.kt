package com.example.anilist.ui.mediadetails

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.Review
import com.example.anilist.data.models.Staff
import com.example.anilist.data.repository.MediaDetailsRepository

private const val STARTING_KEY = 1

class StaffPagingSource(
    private val mediaId: Int,
    private val mediaDetailsRepository: MediaDetailsRepository
): PagingSource<Int, Staff>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Staff> {
        val start = params.key ?: STARTING_KEY
        val data = mediaDetailsRepository.fetchStaffList(mediaId, start, params.loadSize)
        return LoadResult.Page(
            data = data,
            prevKey = if (start == STARTING_KEY) null else start - 1,
            nextKey = if (data.isNotEmpty()) start + 1 else null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Staff>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchor position is the last index that successfully fetched data
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }
}