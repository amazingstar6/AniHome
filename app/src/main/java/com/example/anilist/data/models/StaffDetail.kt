package com.example.anilist.data.models

data class StaffDetail(
    val id: Int = -1,
    val coverImage: String = "",
    val userPreferredName: String = "",
    val alternativeNames: List<String> = emptyList(),
    val isFavourite: Boolean = false,
    val isFavouriteBlocked: Boolean = false,
    val favourites: Int = -1,
    val language: String = "",
    val description: String = "",
    val voicedCharacters: List<Character> = emptyList(),
    val animeStaffRole: List<CharacterMediaConnection> = emptyList(),
    val mangaStaffRole: List<CharacterMediaConnection> = emptyList()
)
