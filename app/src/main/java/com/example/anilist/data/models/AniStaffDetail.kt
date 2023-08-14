package com.example.anilist.data.models

data class AniStaffDetail(
    val id: Int = -1,
    val coverImage: String = "",
    val userPreferredName: String = "",
    val alternativeNames: List<String> = emptyList(),
    val isFavourite: Boolean = false,
    val isFavouriteBlocked: Boolean = false,
    val favourites: Int = -1,
    val language: String = "",
    val description: String = "",
    val voicedCharacters: List<CharacterWithVoiceActor> = emptyList(),
    val animeStaffRole: List<AniCharacterMediaConnection> = emptyList(),
    val mangaStaffRole: List<AniCharacterMediaConnection> = emptyList(),
)
