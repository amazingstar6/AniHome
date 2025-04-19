package com.example.anilist.data.repository.mymedia

import com.example.anilist.data.models.AniPersonalMediaStatus
import com.example.anilist.data.models.AniResult
import com.example.anilist.data.models.Media
import com.example.anilist.data.models.StatusUpdate

interface MyMediaRepository {
    suspend fun getMyMedia(
        isAnime: Boolean,
        useNetworkFirst: Boolean,
    ): AniResult<Pair<Map<AniPersonalMediaStatus, List<Media>>, Boolean>>

    suspend fun updateProgress(statusUpdate: StatusUpdate): AniResult<Media>

    suspend fun deleteEntry(entryListId: Int): Boolean
}
