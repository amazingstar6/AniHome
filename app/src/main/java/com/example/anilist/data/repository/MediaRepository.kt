package com.example.anilist.data.repository

import androidx.lifecycle.LiveData
import com.example.anilist.data.models.Media

interface MediaRepository {
    fun observeMedia(mediaId: Int): LiveData<Media>

    suspend fun refreshMedia(mediaId: Int)
}
