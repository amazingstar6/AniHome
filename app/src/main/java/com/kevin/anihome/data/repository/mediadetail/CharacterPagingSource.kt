package com.kevin.anihome.data.repository.mediadetail

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kevin.anihome.data.models.AniResult
import com.kevin.anihome.data.models.CharacterWithVoiceActor

private const val STARTING_KEY = 1

class CharacterPagingSource(
    private val mediaDetailsRepository: MediaDetailsRepository,
    private val mediaId: Int,
) : PagingSource<Int, CharacterWithVoiceActor>() {
    override fun getRefreshKey(state: PagingState<Int, CharacterWithVoiceActor>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchor position is the last index that successfully fetched data
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CharacterWithVoiceActor> {
        val start = params.key ?: STARTING_KEY
        return when (val data = mediaDetailsRepository.fetchCharacterList(mediaId, start, params.loadSize)) {
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
