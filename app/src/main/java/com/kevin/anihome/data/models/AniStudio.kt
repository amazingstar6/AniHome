package com.kevin.anihome.data.models

data class AniStudio(
    val id: Int = -1,
    val name: String = "",
    val mediaConnected: List<Media> = emptyList(),
    val favourites: Int = -1,
    val isAnimationStudio: Boolean = false,
    val isFavourite: Boolean = false,
    val siteUrl: String = "",
)
