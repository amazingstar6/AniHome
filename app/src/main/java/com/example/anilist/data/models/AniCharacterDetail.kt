package com.example.anilist.data.models

data class AniCharacterDetail(
    val id: Int = -1,
    val userPreferredName: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val fullName: String = "",
    val nativeName: String = "",
    val coverImage: String = "",
    val description: String = "",
    var isFavourite: Boolean = false,
    val isFavoriteBlocked: Boolean = false,
    val favorites: Int = -1,
    val voiceActors: List<AniStaffDetail> = emptyList(),
    val relatedMedia: List<AniCharacterMediaConnection> = emptyList(),
    val alternativeNames: List<String> = emptyList(),
    val alternativeSpoilerNames: List<String> = emptyList(),
)

data class AniCharacterMediaConnection(
    val id: Int = -1,
    val title: String = "",
    val coverImage: String = "",
    val characterRole: String = "",
    val mediaType: String = "",
    val type: AniMediaType = AniMediaType.UNKNOWN,
)
