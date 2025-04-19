package com.example.anilist.data.repository.notificationrepository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.anilist.data.models.AniNotification
import com.example.anilist.data.models.AniResult

private const val STARTING_KEY = 1

class NotificationsPagingSource(
    private val notificationsRepository: NotificationsRepository,
) : PagingSource<Int, AniNotification>() {
    override fun getRefreshKey(state: PagingState<Int, AniNotification>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchor position is the last index that successfully fetched data
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AniNotification> {
        val start = params.key ?: STARTING_KEY
        val data =
            notificationsRepository.getNotifications(page = start, pageSize = params.loadSize)
        return when (data) {
            is AniResult.Failure -> LoadResult.Error(Exception(data.error))
            is AniResult.Success ->
                LoadResult.Page(
                    data = data.data,
                    prevKey = if (start == STARTING_KEY) null else start - 1,
                    nextKey = if (data.data.isNotEmpty()) start + 1 else null,
                )
        }
    }
}
