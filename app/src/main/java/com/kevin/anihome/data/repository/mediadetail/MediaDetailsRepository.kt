package com.kevin.anihome.data.repository.mediadetail

import com.kevin.anihome.data.models.AniLikeAbleType
import com.kevin.anihome.data.models.AniResult
import com.kevin.anihome.data.models.AniReview
import com.kevin.anihome.data.models.AniStaff
import com.kevin.anihome.data.models.CharacterWithVoiceActor
import com.kevin.anihome.data.models.Media

interface MediaDetailsRepository {
    suspend fun fetchMedia(mediaId: Int): AniResult<Media>

    suspend fun fetchStaffList(
        mediaId: Int,
        page: Int,
        pageSize: Int,
    ): AniResult<List<AniStaff>>

    suspend fun fetchCharacterList(
        mediaId: Int,
        page: Int,
        pageSize: Int,
    ): AniResult<List<CharacterWithVoiceActor>>

    suspend fun fetchReviews(
        mediaId: Int,
        page: Int,
        pageSize: Int,
    ): AniResult<List<AniReview>>

    suspend fun toggleFavourite(
        type: AniLikeAbleType,
        id: Int,
    ): AniResult<Boolean>
}
