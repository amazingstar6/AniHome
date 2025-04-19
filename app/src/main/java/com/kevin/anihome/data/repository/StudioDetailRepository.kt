package com.kevin.anihome.data.repository

import com.kevin.anihome.GetMediaOfStudioQuery
import com.kevin.anihome.GetStudioDetailsQuery
import com.kevin.anihome.data.models.AniLikeAbleType
import com.kevin.anihome.data.models.AniStudio
import com.kevin.anihome.data.models.Media
import com.kevin.anihome.utils.Apollo
import javax.inject.Inject

private const val TAG = "StudioDetailRepository"

class StudioDetailRepository
    @Inject
    constructor() {
        suspend fun getStudioDetails(id: Int): AniStudio {
            val data = Apollo.apolloClient.query(GetStudioDetailsQuery(id)).execute().data

            return AniStudio(
                id = data?.Studio?.id ?: -1,
                name = data?.Studio?.name ?: "",
                favourites = data?.Studio?.favourites ?: -1,
                isAnimationStudio = data?.Studio?.isAnimationStudio ?: false,
                isFavourite = data?.Studio?.isFavourite ?: false,
                siteUrl = data?.Studio?.siteUrl ?: "",
            )
        }

        suspend fun fetchMedia(
            studioId: Int,
            page: Int,
            pageSize: Int,
        ): List<Media> {
            val data =
                Apollo.apolloClient.query(
                    GetMediaOfStudioQuery(
                        studioId = studioId,
                        page = page,
                        pageSize = pageSize,
                    ),
                ).execute().data
            return data?.Studio?.media?.edges?.filterNotNull()?.map {
                Media(
                    id = it.node?.id ?: -1,
                    title = it.node?.title?.userPreferred ?: "",
                    coverImage = it.node?.coverImage?.extraLarge ?: "",
                )
            }.orEmpty()
        }

        suspend fun toggleFavourite(
            type: AniLikeAbleType,
            id: Int,
        ): Boolean {
            TODO("Not yet implemented")
        }
    }
