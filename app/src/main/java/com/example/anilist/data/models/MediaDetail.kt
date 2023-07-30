package com.example.anilist.data.models

data class MediaDetail(
    val media: Media = Media(),
    val characterWithVoiceActors: List<CharacterWithVoiceActor> = emptyList(),
    val staff: List<Staff> = emptyList(),
)
