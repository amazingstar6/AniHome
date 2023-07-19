package com.example.anilist.data.repository

import androidx.lifecycle.LiveData
import com.example.anilist.data.models.Anime

interface MediaRepository {
    fun observeMedia(mediaId: Int): LiveData<Anime>

    suspend fun refreshMedia(mediaId: Int)
}
