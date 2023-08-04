package com.example.anilist.data.repository

import com.example.anilist.GetCurrentUserQuery
import com.example.anilist.utils.Apollo
import javax.inject.Inject

class MainActivityRepository @Inject constructor() {
    suspend fun getUserId(): Int {
        val response = Apollo.apolloClient.query(GetCurrentUserQuery()).execute()
        val data = response.data
        return data?.Viewer?.id ?: -1
    }
}