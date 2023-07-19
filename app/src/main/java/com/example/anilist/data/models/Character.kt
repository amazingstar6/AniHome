package com.example.anilist.data.models

data class Character(
    val id: Int,
    val name: String,
    val coverImage: String,
    val voiceActorName: String,
    val voiceActorCoverImage: String,
    val voiceActorLanguage: String
)