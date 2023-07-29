package com.example.anilist.ui.mediadetails

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.Review
import com.example.anilist.data.repository.HomeRepository
import com.example.anilist.data.repository.MediaDetailsRepository
import com.example.anilist.ui.home.AniMediaSort
import com.example.anilist.ui.home.SearchFilter

private const val STARTING_KEY = 1

class ReviewPagingSource (
    private val mediaDetailsRepository: MediaDetailsRepository,
    private val mediaId: Int
) : PagingSource<Int, Review>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Review> {
        val start = params.key ?: STARTING_KEY
        val data = mediaDetailsRepository.fetchReviews(mediaId, start, params.loadSize)
        return LoadResult.Page(
            data = data,
            prevKey = if (start == STARTING_KEY) null else start - 1,
            nextKey = if (data.isNotEmpty()) start + 1 else null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Review>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchor position is the last index that successfully fetched data
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

}