package com.example.anilist.data.models

data class Character(
    val id: Int = -1,
    val voiceActorId: Int = -1,
    val name: String = "",
    val coverImage: String = "",
    val voiceActorName: String = "",
    val voiceActorCoverImage: String = "",
    val voiceActorLanguage: String = "",
    val role: String = ""
)
