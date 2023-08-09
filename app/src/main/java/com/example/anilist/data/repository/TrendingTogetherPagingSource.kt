package com.example.anilist.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.Media

data class TrendingTogether(
    val trending: List<Media>,
    val popular: List<Media>
)
class TrendingTogetherPagingSource(homeRepository: HomeRepository, isAnime: Boolean):
    PagingSource<Int, TrendingTogether>() {
    override fun getRefreshKey(state: PagingState<Int, TrendingTogether>): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TrendingTogether> {
        TODO("Not yet implemented")
    }

}
