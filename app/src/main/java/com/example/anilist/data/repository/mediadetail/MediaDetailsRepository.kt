package com.example.anilist.data.repository.mediadetail

import com.example.anilist.data.models.AniLikeAbleType
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.AniReview
import com.example.anilist.data.models.AniStaff
import com.example.anilist.data.models.CharacterWithVoiceActor
import com.example.anilist.data.models.Media

interface MediaDetailsRepository {
    suspend fun fetchMedia(
        mediaId: Int
    ): AniResult<Media>

    suspend fun fetchStaffList(mediaId: Int, page: Int, pageSize: Int): AniResult<List<AniStaff>>

    suspend fun fetchCharacterList(
        mediaId: Int,
        page: Int,
        pageSize: Int
    ): AniResult<List<CharacterWithVoiceActor>>

    suspend fun fetchReviews(mediaId: Int, page: Int, pageSize: Int): AniResult<List<AniReview>>

    suspend fun toggleFavourite(type: AniLikeAbleType, id: Int): AniResult<Boolean>
}