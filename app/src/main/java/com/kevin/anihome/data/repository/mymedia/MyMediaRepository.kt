package com.kevin.anihome.data.repository.mymedia

import com.kevin.anihome.data.models.AniPersonalMediaStatus
import com.kevin.anihome.data.models.AniResult
import com.kevin.anihome.data.models.Media
import com.kevin.anihome.data.models.StatusUpdate

interface MyMediaRepository {
    suspend fun getMyMedia(
        isAnime: Boolean,
        useNetworkFirst: Boolean,
    ): AniResult<Pair<Map<AniPersonalMediaStatus, List<Media>>, Boolean>>

    suspend fun updateProgress(statusUpdate: StatusUpdate): AniResult<Media>

    suspend fun deleteEntry(entryListId: Int): Boolean
}
