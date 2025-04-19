package com.kevin.anihome.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kevin.anihome.data.models.Media
import com.kevin.anihome.data.repository.homerepository.HomeRepositoryImpl

data class TrendingTogether(
    val trending: List<Media>,
    val popular: List<Media>,
)

class TrendingTogetherPagingSource(homeRepository: HomeRepositoryImpl, isAnime: Boolean) :
    PagingSource<Int, TrendingTogether>() {
    override fun getRefreshKey(state: PagingState<Int, TrendingTogether>): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TrendingTogether> {
        TODO("Not yet implemented")
    }
}
