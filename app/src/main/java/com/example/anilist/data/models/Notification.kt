package com.example.anilist.data.models

data class Notification(
    val type: String = "",
    val context: List<String?>? = emptyList(),
    val createdAt: Int = -1,
    val image: String = "",
    val airedEpisode: Int = -1,
    val title: String = "",
)
