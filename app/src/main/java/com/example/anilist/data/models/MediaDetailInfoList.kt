package com.example.anilist.data.models

data class MediaDetailInfoList(
    val format: String = "",
    val status: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val duration: Int = -1,
    val country: String = "",
    val source: String = "",
    val hashtag: String = "",
    val licensed: Boolean? = false,
    val updatedAt: String = "",
    val synonyms: List<String> = emptyList(),
    val nsfw: Boolean? = false
)

