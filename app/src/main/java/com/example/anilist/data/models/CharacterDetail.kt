package com.example.anilist.data.models

data class CharacterDetail(
    val id: Int = -1,
    val userPreferredName: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val fullName: String = "",
    val nativeName: String = "",
    val coverImage: String = "",
    val description: String = "",
    val isFavorite: Boolean = false,
    val isFavoriteBlocked: Boolean = false,
    val favorites: Int = -1,
    val voiceActors: List<StaffDetail> = emptyList(),
    val relatedMedia: List<CharacterMediaConnection> = emptyList(),
    val alternativeNames: List<String> = emptyList(),
    val alternativeSpoilerNames: List<String> = emptyList()
)

data class CharacterMediaConnection(
    val id: Int = -1,
    val title: String = "",
    val coverImage: String = "",
    val characterRole: String = ""
)
