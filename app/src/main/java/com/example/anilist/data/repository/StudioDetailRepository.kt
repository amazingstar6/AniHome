package com.example.anilist.data.repository

import android.util.Log
import com.example.anilist.GetMediaOfStudioQuery
import com.example.anilist.GetStudioDetailsQuery
import com.example.anilist.data.models.AniStudio
import com.example.anilist.data.models.Media
import com.example.anilist.utils.Apollo
import javax.inject.Inject

private const val TAG = "StudioDetailRepository"
class StudioDetailRepository @Inject constructor() {
    suspend fun getStudioDetails(id: Int): AniStudio {
        val data = Apollo.apolloClient.query(GetStudioDetailsQuery(id)).execute().data

        return AniStudio(
            id = data?.Studio?.id ?: -1,
            name = data?.Studio?.name ?: "",
            favourites = data?.Studio?.favourites ?: -1,
            isAnimationStudio = data?.Studio?.isAnimationStudio ?: false,
            isFavourite = data?.Studio?.isFavourite ?: false,
            siteUrl = data?.Studio?.siteUrl ?: ""
        )
    }

    suspend fun fetchMedia(studioId: Int, page: Int, pageSize: Int): List<Media> {
        Log.d(TAG, "Hello, $studioId, $page, $pageSize")
        val data = Apollo.apolloClient.query(
            GetMediaOfStudioQuery(
                studioId = studioId,
                page = page,
                pageSize = pageSize
            )
        ).execute().data
        Log.d(TAG, "${data?.Studio?.media?.edges}")
        return data?.Studio?.media?.edges?.filterNotNull()?.map {
            Media(
                id = it.node?.id ?: -1,
                title = it.node?.title?.userPreferred ?: "",
                coverImage = it.node?.coverImage?.extraLarge ?: ""
            )
        }.orEmpty()
    }
}